# DT Tool

A tool that helps you to manage Dependency Track content better.

---

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)][license]
[![build workflow](https://github.com/elomagic/dt-tool/actions/workflows/maven.yml/badge.svg)](https://github.com/elomagic/dt-tool/actions)
[![GitHub issues](https://img.shields.io/github/issues-raw/elomagic/dt-tool)](https://github.com/elomagic/dt-tool/issues)
[![Latest](https://img.shields.io/github/release/elomagic/dt-tool.svg)](https://github.com/elomagic/dt-tool/releases)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/elomagic/dt-tool/graphs/commit-activity)
[![Buymeacoffee](https://badgen.net/badge/icon/buymeacoffee?icon=buymeacoffee&label)](https://www.buymeacoffee.com/elomagic)


## Using the library

### Requirements

* Installed Java 21 or higher
* Download the latest version of DT-Tool https://github.com/elomagic/dt-tool/releases

### Configuration

#### File ```${sys:user.home}/.dt-tool/configuration.json5```

The configuration `file, named ```configuration.json5```, must be placed in a sub folder named ```.dt-tool``` of the users home folder.

```json5
{
    // Base URL of Dependency Track
    "baseUrl": "https://dependencytrackapi.local",
    // API key to access REST API of the Dependency Track
    "apiKey": "<API-KEY>",
}
```

### Execute

Execute following line to see all supported options:

```shell
dt-tool.sh --help
```

#### Example - Print outdated SNAPSHOTS (30 days and older)

TODO UPDATE SYNTAX

```shell
dt-tool.sh -cp
```

#### Example - Print invalid license IDs

TODO UPDATE SYNTAX

```shell
dt-tool.sh -lic -v
```

## Contributing

Pull requests and stars are always welcome. For bugs and feature requests, [please create an issue](../../issues/new).

### Versioning

Versioning follows the semantic of [Semantic Versioning 2.0.0](https://semver.org/)

## License

The dt-tool is distributed under [Apache License, Version 2.0][license]

[license]: https://www.apache.org/licenses/LICENSE-2.0