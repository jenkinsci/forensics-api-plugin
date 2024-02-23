/* global jQuery3, proxy, echartsJenkinsApi, bootstrap5 */
(function () {
    function fillForensics(trendConfiguration, jsonConfiguration) {
      const type = jsonConfiguration['chartType'];
      if (type) {
        trendConfiguration.find('#' + type + '-forensics').prop('checked', true);
      }
    }

    function saveForensics(trendConfiguration) {
      return {
        'chartType': trendConfiguration.find('input[name=chartType-forensics]:checked').attr('id').replace('-forensics', '')
      };
    }

    echartsJenkinsApi.configureTrend('forensics', fillForensics, saveForensics)
})();
