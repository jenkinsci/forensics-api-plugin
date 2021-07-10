A pipeline script to create a history of a git project.

```groovy
node {
    stage('Checkout') {
        def url = "https://github.com/jenkinsci/forensics-api-plugin.git"
        env.COUNT = sh(script: "git ls-remote -t --refs $url | cut -f1 | wc -l", returnStdout: true).trim()
        if (env.COUNT.toInteger() > env.BUILD_NUMBER.toInteger()) {
            echo "Build: ${BUILD_NUMBER}"
            env.HASH = sh(script: "git ls-remote -t --refs $url | cut -f1 | head -n ${BUILD_NUMBER} | tail -1", returnStdout: true).trim()
            echo "Hash: ${HASH}"
            checkout([$class           : 'GitSCM',
                      branches         : [[name: "${HASH}"]],
                      userRemoteConfigs: [[url: "$url"]]])
            gitDiffStat()
            mineRepository()
            build(job: env.JOB_NAME, wait: false)
        } else {
            echo "Stopping after ${COUNT} tags"
        }
    }
}
```
