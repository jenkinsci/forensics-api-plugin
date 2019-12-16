package io.jenkins.plugins.forensics.blame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.ResourceTest;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link BlamesXmlStream}.
 *
 * @author Ullrich Hafner
 */
class BlamesXmlStreamTest extends ResourceTest {
    private static final String WORKSPACE = "/var/data/workspace/pipeline-analysis-model/";
    private static final String REPORT_SRC = "src/main/java/edu/hm/hafner/analysis/Report.java";
    private static final String REPORT = WORKSPACE + REPORT_SRC;
    private static final String FILTERED_LOG_SRC = "src/main/java/edu/hm/hafner/analysis/FilteredLog.java";
    private static final String FILTERED_LOG = WORKSPACE + FILTERED_LOG_SRC;

    @Test
    void shouldReadBlamesOfWarnings520() {
        BlamesXmlStream blamesReader = new BlamesXmlStream();

        Blames restored520 = blamesReader.read(getResourceAsFile("fileBlame-5.2.0.xml"));
        assertThatBlamesAreCorrect(restored520);
        blamesReader.write(Paths.get("/tmp/serialized.xml"), restored520);
    }

    @Test
    void shouldReadBlamesOfForensics070() {
        BlamesXmlStream blamesReader = new BlamesXmlStream();

        Blames restored070 = blamesReader.read(getResourceAsFile("fileBlame-0.7.0.xml"));
        assertThatBlamesAreCorrect(restored070);
    }

    @Test
    void shouldReadBlamesOfForensics062() {
        BlamesXmlStream blamesReader = new BlamesXmlStream();

        Blames restored062 = blamesReader.read(getResourceAsFile("fileBlame-0.6.2.xml"));
        assertThatBlamesAreCorrect(restored062);
    }

    @Test
    void shouldReadAndWriteBlames() {
        BlamesXmlStream blamesReader = new BlamesXmlStream();

        Blames restored070 = blamesReader.read(getResourceAsFile("fileBlame-0.7.0.xml"));
        Path saved = createTempFile();
        blamesReader.write(saved, restored070);

        Blames newFormat = blamesReader.read(saved);
        assertThatBlamesAreCorrect(newFormat);

        // blamesReader.write(Paths.get("/tmp/serialized.xml"), newFormat);
    }

    private void assertThatBlamesAreCorrect(final Blames blames) {
        assertThat(blames.getFiles()).contains(REPORT, FILTERED_LOG);

        FileBlame report = new FileBlame(REPORT_SRC);
        report.setCommit(768, "11d9cdf38bd029d970705b1151aef910cd873044");
        report.setName(768, "Ulli Hafner");
        report.setEmail(768, "ullrich.hafner@gmail.com");
        report.setCommit(83, "2fcc7335c5b3570d5c624a94d43dc886e305b21a");
        report.setName(83, "Ulli Hafner");
        report.setEmail(83, "ullrich.hafner@gmail.com");
        report.setCommit(101, "cb183f0e9d97a49584f9fa4f932dceb9b24e9586");
        report.setName(101, "Ulli Hafner");
        report.setEmail(101, "ullrich.hafner@gmail.com");

        assertThat(blames.getBlame(REPORT)).isEqualTo(report);
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("test", ".blames");
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
