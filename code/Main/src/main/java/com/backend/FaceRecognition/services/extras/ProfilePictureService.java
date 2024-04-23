package com.backend.FaceRecognition.services.extras;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ProfilePicture;
import com.backend.FaceRecognition.repository.ProfilePictureRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfilePictureService {
    private final ApplicationUserService applicationUserService;
    private final ProfilePictureRepository profilePictureRepository;
    private final JwtService service;
    @Value("${profile.picture.maxFileSizeKB}")
    private int maxFileSizeKB;

    public ProfilePictureService(ApplicationUserService applicationUserService, ProfilePictureRepository profilePictureRepository, JwtService service) {
        this.applicationUserService = applicationUserService;
        this.profilePictureRepository = profilePictureRepository;
        this.service = service;
    }
    private boolean isValidProfilePicture(MultipartFile file) {
        return file != null && file.getSize() <= maxFileSizeKB * 1024L;
    }
    public ResponseEntity<Response> uploadProfilePicture(String bearer, MultipartFile file){
        if (isValidProfilePicture(file)) {
            String id = service.getId(service.extractTokenFromHeader(bearer));
            ApplicationUser user = applicationUserService.findUser(id).get();
            ProfilePicture profilePicture = new ProfilePicture();
            profilePicture.setUser(user);
            try {
                profilePicture.setImageData(file.getBytes());
                profilePicture = profilePictureRepository.save(profilePicture);
                user.setProfilePictureId(String.valueOf(profilePicture.getId()));
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(new Response("Bad Image"));
            }
        }
        return ResponseEntity.badRequest().body(new Response("Image too Large at most 100KB"));
    }
    public ResponseEntity<byte[]> getProfilePicture(String bearer){
        String id = service.getId(service.extractTokenFromHeader(bearer));
        ProfilePicture pp = profilePictureRepository.findByUser_Id(id).orElse(null);
        return pp!=null?ResponseEntity.ok(pp.getImageData()):ResponseEntity.notFound().build();
    }



}
