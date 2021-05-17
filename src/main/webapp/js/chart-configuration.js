/* global jQuery3, view, echartsJenkinsApi, bootstrap5 */
function fillForensics (trendConfiguration, jsonConfiguration) {
    const type = jsonConfiguration['chartType'];
    if (type) {
        trendConfiguration.find('#' + type).prop('checked', true);
    }
}

function saveForensics (trendConfiguration, jsonConfiguration) {
    jsonConfiguration['chartType'] = trendConfiguration.find('input[name=chartType]:checked').attr('id');
}
