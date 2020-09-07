package io.jenkins.plugins.forensics.util;

/**
 * Helper class used to keep track of lines of code changes for a file concerning a specific commit.
 *
 * @author Giulia Del Bravo
 */
public class LocChanges {

    private int totalLoc;
    private final String commitId;
    private int addedLines;
    private int deletedLines;

        public LocChanges(String commitId){
            this.commitId = commitId;
            totalLoc = 0;
            addedLines = 0;
            deletedLines = 0;
        }

        public void addTotalLoc(int newLines) {
            totalLoc += newLines;
        }

        public void addAddedLines(int addedLines) {
            this.addedLines += addedLines;
            totalLoc+= addedLines;
        }

        public void addDeletedLines(int removedLines) {
            this.deletedLines += removedLines;
            totalLoc-=removedLines;
        }

        public int getTotalLoc() {
            return totalLoc;
        }

        public int getAddedLines(){
            return addedLines;
        }

        public int getDeletedLines() {
            return deletedLines;
        }

        public String getCommitId() {
            return commitId;
        }

}
