package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AddedVersusDeletedLinesForensicsSeriesBuilderTest {

    @Test
    void computeSeries() {
        Map<String, Integer> series = AddedVersusDeletedLinesForensicsSeriesBuilder.computeAddedVsDeletedSeries(createCommitStatistics(new int[][]{{10,0}, {10,5}, {1,0}}));
        assertThat(series.get("added")).isEqualTo(21);
        assertThat(series.get("deleted")).isEqualTo(5);
        Map<String, Integer> series2 = AddedVersusDeletedLinesForensicsSeriesBuilder.computeAddedVsDeletedSeries(createCommitStatistics(new int[][]{{1,0}}));
        assertThat(series2.get("added")).isEqualTo(1);
        assertThat(series2.get("deleted")).isEqualTo(0);
        Map<String, Integer> series3 = AddedVersusDeletedLinesForensicsSeriesBuilder.computeAddedVsDeletedSeries(createCommitStatistics(new int[][]{{0,1}}));
        assertThat(series3.get("added")).isEqualTo(0);
        assertThat(series3.get("deleted")).isEqualTo(1);
        Map<String, Integer> series4 = AddedVersusDeletedLinesForensicsSeriesBuilder.computeAddedVsDeletedSeries(createCommitStatistics(new int[][]{{0,0}}));
        assertThat(series4.get("added")).isEqualTo(0);
        assertThat(series4.get("deleted")).isEqualTo(0);
    }

    private CommitStatistics createCommitStatistics(int[][] commitsAddedAndDeletedValues){
        List<CommitDiffItem> commits = new ArrayList<>();
        for(int i = 0; i< commitsAddedAndDeletedValues.length; i++){
            CommitDiffItem item = new CommitDiffItem( i+"", "author", i);
            item.addLines(commitsAddedAndDeletedValues[i][0]).deleteLines(commitsAddedAndDeletedValues[i][1]);
            commits.add(item);
        }
        return new CommitStatistics(commits);
    }
}