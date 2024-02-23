/* global jQuery3, proxy, echartsJenkinsApi, bootstrap5 */
(function () {
    function fillCommitStatistics(trendConfiguration, jsonConfiguration) {
        const type = jsonConfiguration['chartType'];
        if (type) {
            trendConfiguration.find('#' + type + '-commit-statistics').prop('checked', true);
        }
    }

    function saveCommitStatistics(trendConfiguration) {
        return {
            'chartType': trendConfiguration.find('input[name=chartType-commit-statistics]:checked').attr('id').replace('-commit-statistics', '')
        };
    }

    echartsJenkinsApi.configureTrend('commit-statistics', fillCommitStatistics, saveCommitStatistics);
})();
