package webchat.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webchat.invalidLinkException.InvalidLinkException;
import webchat.model.User;
import webchat.repository.UserRepository;
import webchat.serializableClasses.Requests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.util.StringUtils.getFilenameExtension;

@RestController
public class FileController {
    public final String fileRoute;
    private final UserRepository userRepository;
    public FileController(@Value("${webchat.file-controller.avatars}") String fileRoute, UserRepository userRepository) throws IOException {
        this.fileRoute = fileRoute;
        this.userRepository = userRepository;
        Files.createDirectories(Path.of(fileRoute));
    };

    @PostMapping("/avatars/upload")
    public ImageEntity uploadImage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                   @RequestParam MultipartFile file) throws IOException {
        User user = userRepository.findByName(currentUser.getUsername()).orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        String imagePath = getNewImageUrl(user.getName()+"."+getFilenameExtension(file.getOriginalFilename()));
        byte[] bytes = file.getBytes();
        Files.write(Path.of(imagePath), bytes);
        user.setAvatarLink(imagePath);
        userRepository.save(user);
        return new ImageEntity(imagePath);
    }

    @PostMapping("/avatars/change")
    public ImageEntity changeAvatar(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                    @RequestParam MultipartFile file) throws IOException{
        User user = userRepository.findByName(currentUser.getUsername()).orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        String imagePath = getNewImageUrl(user.getName()+"."+getFilenameExtension(file.getOriginalFilename()));
        Files.delete(Path.of(user.getAvatarLink()));
        byte[] bytes = file.getBytes();
        Files.write(Path.of(imagePath), bytes);
        user.setAvatarLink(imagePath);
        userRepository.save(user);
        return new ImageEntity(imagePath);
    }

    @PostMapping("/avatars/delete")
    public ImageEntity deleteAvatar(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) throws IOException{
        User user = userRepository.findByName(currentUser.getUsername()).orElseThrow(() -> new UsernameNotFoundException(currentUser.getUsername()));
        Files.delete(Path.of(user.getAvatarLink()));
        user.setAvatarLink(null);
        userRepository.save(user);
        return new ImageEntity(user.getAvatarLink());
    }

    @PostMapping("/avatars/get_avatar")
    public ResponseEntity<byte[]> getFile(@RequestBody Requests.UserAvatarRequestBody userAvatarRequestBody){
        User user = userRepository.findByName(userAvatarRequestBody.userName()).orElseThrow(() -> new UsernameNotFoundException(userAvatarRequestBody.userName()));
        String fileLink = user.getAvatarLink();
        if(!fileLink.startsWith(fileRoute))
            throw new InvalidLinkException(fileLink, new Throwable(Path.of("/" + fileLink).toAbsolutePath() + "_fileRoute"));

        try{
            String filenameExtension = getFilenameExtension(fileLink);
            if(filenameExtension == null)
                throw new InvalidLinkException(fileLink + ", no extension");



            return ResponseEntity
                    .ok()
                    .contentType(new MediaType("image", filenameExtension))
                    .body(Files.readAllBytes(Path.of(fileLink)));
        }catch(IllegalArgumentException | NullPointerException | IOException | StringIndexOutOfBoundsException e){
            throw new InvalidLinkException(fileLink, e);
        }
    }

    private String getNewImageUrl(String userName){
        return fileRoute + "/" + userName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ImageEntity{
        private String url;
    }
}
