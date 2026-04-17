import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ServerJarTransform implements TransformAction<TransformParameters.None> {

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File inputFile = getInputArtifact().get().getAsFile();

        try (ZipFile zipFile = new ZipFile(inputFile)) {
            ZipEntry foundEntry = null;

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith("META-INF/versions/") && name.endsWith(".jar")) {
                    foundEntry = entry;
                    break;
                }
            }

            if (foundEntry == null) {
                throw new RuntimeException("No inner server JAR found in " + inputFile.getName());
            }

            String outputName = new File(foundEntry.getName()).getName();
            File outputFile = outputs.file(outputName);

            try (InputStream is = zipFile.getInputStream(foundEntry);
                 FileOutputStream os = new FileOutputStream(outputFile))
            {

                is.transferTo(os);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract inner server JAR", e);
        }
    }
}
