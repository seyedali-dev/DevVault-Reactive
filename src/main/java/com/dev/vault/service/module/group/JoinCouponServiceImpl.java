package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.model.project.JoinCoupon;
import com.dev.vault.model.project.Project;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.JoinCouponRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.service.interfaces.AuthenticationService;
import com.dev.vault.service.interfaces.JoinCouponService;
import com.dev.vault.util.repository.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

/**
 * Service implementation for generating join project request coupon.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JoinCouponServiceImpl implements JoinCouponService {

    private final JoinCouponRepository joinCouponRepository;
    private final ProjectRepository projectRepository;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final RepositoryUtils repositoryUtils;

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
    public String generateOneTimeJoinCoupon(Long projectId, Long requestingUserId) {
        // Get the project with the given ID from the database
        Project project = repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);

        // Get the requesting user (the user who is requesting to join the project)
        User requestingUser = repositoryUtils.findUserById_OrElseThrow_ResourceNoFoundException(requestingUserId);

        // Check if the current user is the leader or admin of the specific project
        checkLeaderOrAdminOfProject(project, authenticationService.getCurrentUser());

        // Check if a join coupon has already been generated for the requesting user and project, and if so, throw an exception
        checkJoinCouponAlreadyGenerated(requestingUser, project);

        // Create a new join coupon object with the requesting user, leader, and project
        String randomCoupon = generateRandomCoupon(project.getProjectName(), project.getMemberCount(), requestingUser);
        User leader = project.getLeader();
        JoinCoupon joinCoupon = new JoinCoupon(requestingUser, leader, project, randomCoupon);

        joinCouponRepository.save(joinCoupon);

        return joinCoupon.getCoupon();
    }

    /**
     * Checks if the current user is the leader or admin of the specific project.
     *
     * @param project the project to check the leader or admin for
     * @throws NotLeaderOfProjectException if the current user is not the leader or admin of the specific project
     */
    private void checkLeaderOrAdminOfProject(Project project, User user) {
        if (!projectUtils.isLeaderOrAdminOfProject(project, user))
            throw new NotLeaderOfProjectException("❌ You are not the leader or admin of this project ❌");
    }

    /**
     * Checks if a join coupon has already been generated for the requesting user and project, and if so, throws an exception.
     *
     * @param requestingUser the user who is requesting to join the project
     * @param project        the project to check the join coupon for
     * @throws ResourceAlreadyExistsException if a join coupon has already been generated for the requesting user and project
     */
    private void checkJoinCouponAlreadyGenerated(User requestingUser, Project project) {
        Optional<JoinCoupon> foundCoupon = joinCouponRepository.findByRequestingUserAndProject(requestingUser, project);
        if (foundCoupon.isPresent())
            throw new ResourceAlreadyExistsException("A coupon is already generated for: " + requestingUser.getUsername());
    }

    /**
     * Generates a random join coupon string for the requesting user to join the specified project.
     *
     * @param projectName    the name of the project for which the join coupon is being generated.
     * @param memberCount    the number of members currently in the project.
     * @param requestingUser the user who is requesting to join the project.
     * @return the generated join coupon string.
     * @throws ResourceNotFoundException if the project cannot be found in the database.
     */
    private String generateRandomCoupon(String projectName, int memberCount, User requestingUser) {
        // Get the project with the given name from the database
        Project project = projectRepository.findByProjectName(projectName)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectID", projectName));

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
        return sb.toString().toUpperCase() +
               "_" +
               project.getProjectId() +
               project.getLeader().getUserId() +
               "_" +
               memberCount +
               requestingUser.getUserId() +
               paddedRandomNum
                ;
    }
}
