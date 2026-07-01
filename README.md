# Welcome!

OpenClinica is an open source software for Electronic Data Capture (EDC) and Clinical Data Management (CDM) used to optimize clinical trial workflow in a smart and secure fashion. Use OpenClinica to:

- Build studies
- Create eCRFs
- Design rules/edit checks
- Schedule patient visits 
- Capture eCRF data from study sites via the web
- Monitor and manage clinical data
- Audit trails and electronic signatures
- Role-based access controls
- Import/Export Data
- Extract data for analysis and reporting
- and much more!

## Getting Started

To get started quickly with a local development environment, run the setup tool:
```bash
python3 setup.py
```
This interactive wizard will verify system prerequisites (Java, Maven, Docker), prompt for necessary configuration values, automatically configure directory structures, and generate a `.env` file for runtime overrides.

### Environment Variable Mapping
The application prioritizes environment variables over file-based properties. The mapping follows an upper snake_case convention. Below is a complete mapping of environment variables to the properties they override:

| Environment Variable | Property Key | Description |
| --- | --- | --- |
| `DB_TYPE` | `dbType` | Database type (e.g., postgres) |
| `DB_USER` | `dbUser` | Database user |
| `DB_PASS` | `dbPass` | Database password |
| `DB` | `db` | Database name |
| `DB_PORT` | `dbPort` | Database port |
| `DB_HOST` | `dbHost` | Database host |
| `FILE_PATH` | `filePath` | Directory for uploaded and generated files |
| `SYS_URL` | `sysURL` | Web address for the system |
| `LOG_DIR` | `log.dir` | Directory for logs |
| `LDAP_ENABLED` | `ldap.enabled` | Enable/disable LDAP authentication |
| `LDAP_HOST` | `ldap.host` | LDAP server host |
| `LDAP_USER_DN` | `ldap.userDn` | LDAP user DN |
| `LDAP_PASSWORD` | `ldap.password` | LDAP password |

- [System requirements](https://docs.openclinica.com/installation/system-requirements)
- [Report an issue](https://jira.openclinica.com/)
- [Release notes](https://docs.openclinica.com/release-notes)
- [Extensions/Contributions](https://community.openclinica.com/extensions)
- [Installation](https://github.com/OpenClinica/OpenClinica/wiki)

## Technical Requirements

<!-- BEGIN GENERATED REQUIREMENTS -->
> **Note:** The following technical requirements are programmatically generated from the build configuration. Manual modifications will be overwritten.

- **Project Version:** 3.18-SNAPSHOT
- **JDK Version:** 17
- **Database:** PostgreSQL 15
- **Application Server:** Tomcat 9.0.83

For non-technical user guides and comprehensive documentation, please visit the [OpenClinica Documentation Portal](https://docs.openclinica.com).
<!-- END GENERATED REQUIREMENTS -->

## Request a feature

To request a feature please submit a ticket on [Jira](https://jira.openclinica.com/) or start a discussion on the [OpenClinica Forum](http://forums.openclinica.com).

## Screenshots
![Imgur](http://i.imgur.com/ACXj3L7.jpg "Home screen") 
![Imgur](http://i.imgur.com/DqHQ05Z.jpg "Subject Matrix")



## License

[GNU LGPL license](https://www.openclinica.com/gnu-lgpl-open-source-license)

