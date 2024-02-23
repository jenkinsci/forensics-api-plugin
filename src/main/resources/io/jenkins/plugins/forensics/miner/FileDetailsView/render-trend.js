/* global jQuery3, proxy, echartsJenkinsApi, bootstrap5 */
(function () {
    proxy.getBuildTrendModel(function (lineModel) {
        const openCommit = function (commit) {
            proxy.getCommitUrl(commit, function (commitUrl) {
                if (commitUrl.responseJSON.startsWith('http')) {
                    window.location.assign(commitUrl.responseJSON);
                }
            });
        };
        echartsJenkinsApi.renderConfigurableZoomableTrendChart('churn-trend-chart', lineModel.responseJSON, null, openCommit);
    });
})();
