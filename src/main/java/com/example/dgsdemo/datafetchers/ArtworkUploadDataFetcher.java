package com.example.dgsdemo.datafetchers;


import com.example.dgsdemo.generated.DgsConstants;
import com.example.dgsdemo.generated.types.Image;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@DgsComponent
public class ArtworkUploadDataFetcher {
    @DgsData(parentType = DgsConstants.MUTATION.TYPE_NAME, field = DgsConstants.MUTATION.AddArtwork)
    public List<Image> uploadArtwork(@InputArgument("showId") Integer showId, @InputArgument("upload") MultipartFile multipartFile) throws IOException {
        Path uploadDir = Paths.get("uploaded-images");
        if(!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path newFile = uploadDir.resolve("show-" + showId + "-" + UUID.randomUUID() + multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")));
        try(OutputStream outputStream = Files.newOutputStream(newFile)) {
            outputStream.write(multipartFile.getBytes());
        }

        return Files.list(uploadDir)
                .filter(f -> f.getFileName().toString().startsWith("show-" + showId))
                .map(f -> f.getFileName().toString())
                .map(fileName -> Image.newBuilder().url(fileName).build()).collect(Collectors.toList());

    }

}
