def configurations = [
  [ platform: "linux", jdk: "21" ],
  [ platform: "windows", jdk: "17" ]
]

def params = [
    failFast: false,
    pit: [skip: false, sourceCodeRetention: 'MODIFIED'],
    configurations: configurations,
    checkstyle: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    pmd: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    jacoco: [sourceCodeRetention: 'MODIFIED']
    ]

buildPlugin(params)
