package io.jenkins.plugins.forensics.util;

/**
 * Helper class used to keep track of lines of code changes for a file concerning a specific commit.
 *
 * @author Giulia Del Bravo
 */
public class LocChanges {

    private int totalLoc;
    private final String commitId;
    private int totalAddedLines;
    private int deletedLines;

        public LocChanges(final String commitId){
            this.commitId = commitId;
            totalLoc = 0;
            totalAddedLines = 0;
            deletedLines = 0;
        }

        public void updateLocChanges(final int addedLines, final int removedLines) {
            totalLoc += addedLines;
            totalLoc -= removedLines;
            this.totalAddedLines += addedLines;
            this.deletedLines += removedLines;
        }

        public int getTotalLoc() {
            return totalLoc;
        }

        public int getTotalAddedLines(){
            return totalAddedLines;
        }

        public int getDeletedLines() {
            return deletedLines;
        }

        public String getCommitId() {
            return commitId;
        }

}
