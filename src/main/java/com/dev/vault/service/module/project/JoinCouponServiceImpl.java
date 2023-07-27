package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.entity.project.JoinCoupon;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.repository.project.JoinCouponReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.service.interfaces.project.JoinCouponService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * Service implementation for generating join project request coupon.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JoinCouponServiceImpl implements JoinCouponService {

    private final UserReactiveRepository userReactiveRepository;
    private final JoinCouponReactiveRepository joinCouponReactiveRepository;
    private final ProjectReactiveRepository projectRepository;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;


    /**
     * Generates a one-time join coupon for the specified project and requesting user.
     *
     * @param projectId        the ID of the project to generate the join coupon for
     * @param requestingUserId the ID of the user who is requesting to join the project
     * @return the generated join coupon string
     * @throws ResourceNotFoundException      if the project or user cannot be found
     * @throws NotLeaderOfProjectException    if the current user is not the leader or admin of the specific project
     * @throws ResourceAlreadyExistsException if a join coupon has already been generated for the requesting user and project
     */
    @Override
    @Transactional
    public Mono<String> generateOneTimeJoinCoupon(String projectId, String requestingUserId) {
        // Get the project with the given ID from the database
        return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                .flatMap(project -> {
                    // Get the requesting user (the user who is requesting to join the project)
                    return reactiveRepositoryUtils.findUserById_OrElseThrow_ResourceNotFoundException(requestingUserId)
                            .flatMap(requestingUser -> {
                                // Check if the current user is the leader or admin of the specific project
                                return authenticationService.getCurrentUserMono()
                                        .flatMap(currentUser ->
                                                checkLeaderOrAdminOfProject(project, currentUser)
                                                        .flatMap(isLeaderOrAdmin -> {
                                                            // Check if a join coupon has already been generated for the requesting user and project, and if so, throw an exception
                                                            return checkJoinCouponAlreadyGenerated(requestingUser, project)
                                                                    .flatMap(foundJoinCoupon -> {
                                                                        if (!foundJoinCoupon) {
                                                                            // Create a new join coupon object with the requesting userID, leaderEmail, and projectID
                                                                            return generateRandomCoupon(project.getProjectName(), project.getMemberCount(), requestingUser)
                                                                                    .flatMap(randomCoupon -> {
                                                                                        String leaderEmail = project.getLeaderEmail();
                                                                                        JoinCoupon generatedJoinCoupon = new JoinCoupon(
                                                                                                requestingUser.getUserId(),
                                                                                                leaderEmail,
                                                                                                project.getProjectId(),
                                                                                                randomCoupon
                                                                                        );
                                                                                        // Save the generated join coupon to the database
                                                                                        return joinCouponReactiveRepository.save(generatedJoinCoupon)
                                                                                                .thenReturn(generatedJoinCoupon.getCoupon());
                                                                                    });
                                                                        } else {
                                                                            log.error("❌ A coupon is already generated for: {{}}", requestingUser.getUsername());
                                                                            return Mono.error(new ResourceAlreadyExistsException("A coupon is already generated for: " + requestingUser.getUsername()));
                                                                        }
                                                                    });
                                                        })
                                        );
                            });
                }).doOnError(error -> log.error("Error generating join coupon: {}", error.getMessage()));
    }


    /**
     * Checks if the current user is the leader or admin of the specific project.
     *
     * @param project the project to check the leader or admin for
     * @param user    the user to check if they are the leader or admin of the project
     * @return a Mono<Boolean> that emits a true value if the user is a leader or admin of the project
     * @throws NotLeaderOfProjectException if the current user is not the leader or admin of the specific project
     */
    public Mono<Boolean> checkLeaderOrAdminOfProject(Project project, User user) {
        return projectUtils.isLeaderOrAdminOfProject(project, user)
                .flatMap(isLeaderOrAdmin -> {
                            if (isLeaderOrAdmin)
                                return Mono.just(true);
                            else
                                return Mono.error(new NotLeaderOfProjectException("❌ You are not the leader or admin of this project ❌"));
                        }
                );
    }


    /**
     * Checks if a join coupon has already been generated for the requesting user and project, and if so, throws an exception.
     *
     * @param user    the user who is requesting to join the project
     * @param project the project to check the join coupon for
     * @return a Mono<Boolean> that emits a true value if a join coupon has already been generated for the requesting user and project
     * @throws ResourceAlreadyExistsException if a join coupon has already been generated for the requesting user and project
     */
    private Mono<Boolean> checkJoinCouponAlreadyGenerated(User user, Project project) {
        return joinCouponReactiveRepository.findByRequestingUserIdAndProjectId(user.getUserId(), project.getProjectId())
                .hasElement().map(found -> {
                    log.info("found any join coupon?: {{}}", found);
                    return found;
                });
    }


    /**
     * Generates a random join coupon string for the requesting user to join the specified project.
     *
     * @param projectName    the name of the project for which the join coupon is being generated.
     * @param memberCount    the number of members currently in the project.
     * @param requestingUser the user who is requesting to join the project.
     * @return a Mono of String that emits the generated join coupon string.
     * @throws ResourceAlreadyExistsException if the user asks for a coupon, for him/herself.
     * @throws ResourceNotFoundException      if the project cannot be found in the database.
     */
    private Mono<String> generateRandomCoupon(String projectName, int memberCount, User requestingUser) {
        return authenticationService.getCurrentUserMono()
                .flatMap(user -> {
                    if (requestingUser.getUserId().equals(user.getUserId()))
                        return Mono.error(new ResourceAlreadyExistsException("You are already a member! no need for a new coupon :)"));
                    else {
                        // Get the project with the given name from the database
                        return projectRepository.findByProjectName(projectName)
                                .flatMap(project -> {
                                    Random random = new Random();
                                    int paddedRandomNum = random.nextInt(91) + 10;

                                    // Append the random 4 characters of the project name to the coupon string
                                    StringBuilder sb = new StringBuilder();

                                    for (int i = 0; i < 4; i++) {
                                        int randomIndex = random.nextInt(projectName.length());
                                        char randomChar = projectName.charAt(randomIndex);
                                        sb.append(randomChar);
                                    }

                                    // Append the project ID, leader ID, member count and requesting user's ID to the coupon string Return the generated coupon string
                                    return userReactiveRepository.findByEmail(project.getLeaderEmail())
                                            .flatMap(leader ->
                                                    Mono.just(sb.toString().toUpperCase() +
                                                              "_" +
                                                              project.getProjectId().substring(2, 4) +
                                                              leader.getUserId().substring(2, 4) +
                                                              "_" +
                                                              memberCount +
                                                              requestingUser.getUserId().substring(2, 4) +
                                                              paddedRandomNum
                                                    )
                                            );
                                }).switchIfEmpty(Mono.error(new ResourceNotFoundException("Project", "ProjectID", projectName)));
                    }
                });
    }
}
