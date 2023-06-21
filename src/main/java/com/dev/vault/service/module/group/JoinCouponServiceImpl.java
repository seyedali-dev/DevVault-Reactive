package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.group.JoinCoupon;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.UserProjectRole;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.group.JoinCouponRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.group.UserProjectRoleRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.JoinCouponService;
import com.dev.vault.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service implementation for generating join project request coupon.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JoinCouponServiceImpl implements JoinCouponService {

    // Dependencies
    private final UserRepository userRepository;
    private final JoinCouponRepository joinCouponRepository;
    private final ProjectRepository projectRepository;
    private final JoinRequestServiceImpl joinRequestService;
    private final AuthenticationService authenticationService;

    /**
     * Generates a one-time join coupon for a user requesting to join a project.
     *
     * @param projectId        the ID of the project for which the join coupon is being generated.
     * @param requestingUserId the ID of the user who is requesting to join the project.
     * @return the generated join coupon string.
     * @throws ResourceNotFoundException      if the project or requesting user cannot be found in the database.
     * @throws ResourceAlreadyExistsException if a join coupon has already been generated for the requesting user and project.
     * @throws DevVaultException              if the requesting user is already a member of the project and is also the leader or admin.
     */
    @Override
    @Transactional
    public String generateOneTimeJoinCoupon(Long projectId, Long requestingUserId) {
        // Get the project with the given ID from the database
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectID", projectId.toString()));

        // Get the requesting user (the user who is requesting to join the project)
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "UserID", requestingUserId.toString()));

        // Get the leader of the project
        User leader = project.getLeader();

        // Check if the current user is the leader or admin of the specific project
        if (!joinRequestService.isLeaderOrAdminOfProject(project)) {
            throw new NotLeaderOfProjectException("❌ You are not the leader or admin of this project ❌");
        }

        // Get the current authenticated user and check if the requesting user is the same as the current user
        User currentUser = authenticationService.getCurrentUser();
        if (requestingUserId.equals(currentUser.getUserId())) {
            List<Role> roles = currentUser.getRoles()
                    .stream().map(Roles::getRole)
                    .filter(role -> role.name().equals("PROJECT_LEADER") || role.name().equals("PROJECT_ADMIN"))
                    .toList();
            throw new ResourceAlreadyExistsException("You are already a member of this project (Roles: " + roles + ")...");
        }

        // Check if a join coupon has already been generated for the requesting user and project, and if so, throw an exception
        Optional<JoinCoupon> foundCoupon = joinCouponRepository.findByRequestingUserAndProject(requestingUser, project);
        if (foundCoupon.isPresent()) {
            throw new ResourceAlreadyExistsException("A coupon is already generated for: " + requestingUser.getUsername());
        }

        // Create a new join coupon object with the requesting user, leader, and project
        String randomCoupon = generateRandomCoupon(project.getProjectName(), project.getMemberCount(), requestingUser);
        JoinCoupon joinCoupon = new JoinCoupon(requestingUser, leader, project, randomCoupon);

        // Save the join coupon object to the database and return the generated coupon string
        joinCouponRepository.save(joinCoupon);
        return joinCoupon.getCoupon();
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

        StringBuilder coupon = new StringBuilder();
        StringBuilder updatedProjectName = new StringBuilder();
        Random random = new Random();

        // Append the first 4 characters of the project name to the coupon string
        for (int i = 0; i < 4; i++) {
            updatedProjectName.append(projectName.charAt(i));
        }

        // Append the project ID, leader ID, and member count to the coupon string
        coupon.append(updatedProjectName.toString().toUpperCase());
        coupon.append(project.getProjectId()).append(project.getLeader().getUserId());
        coupon.append("_");
        coupon.append(memberCount);

        // Append a random number to the coupon string based on the requesting user ID
        coupon.append(requestingUser.getUserId() + random.nextInt(1000));

        // Return the generated coupon string
        return coupon.toString();
    }
}
