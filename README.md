# Dead Drop Service

**Send One-Time Secrets in a secure way**

A Dead Drop service written in Kotlin / KTor.

This service uses [sjcl](https://github.com/bitwiseshiftleft/sjcl) on client-side to encrypt the data, stores the
encrypted data on the server (while never sharing the password with the server), and generates a link to get the data
(once) again.

This is the source-code of [drop.moetz.dev](https://drop.moetz.dev).

## Prerequisites

- Docker is installed on your system
- Docker compose is installed on your system

## Quick Start

The Docker image is available at [hub.docker.com/r/flowmo7/dead-drop](https://hub.docker.com/r/flowmo7/dead-drop).

Get up and running with this basic `docker-compose.yml`:

```yaml
services:
  dead-drop:
    image: "flowmo7/dead-drop:master"
    restart: unless-stopped
    ports:
      - 8080:8080 #Should be behind an SSL reverse proxy
    volumes:
      - /srv/docker/dead-drop/data:/var/dead-drop/data:rw
      - /srv/docker/dead-drop/key:/var/dead-drop/key:rw
```

Then run: `docker-compose up -d`

## Setup

### Security Notice

⚠️ **Important**: This application should always run behind HTTPS. When deploying, ensure `IS_HTTPS` is set to `true`
and the service is behind an HTTPS reverse proxy (like NGINX or Traefik).

### Environment Variables

| Variable                            | Default | Required | Description                                                                                                 |
|-------------------------------------|---------|----------|-------------------------------------------------------------------------------------------------------------|
| `PORT`                              | `8080`  | No       | Port the server listens on                                                                                  |
| `DOMAIN`                            |         | Yes      | Domain where the app is available (e.g., `drop.example.org`). Used to generate share links.                 |
| `IS_HTTPS`                          | `true`  | No       | Set to `true` if the app is behind an HTTPS reverse proxy (it should be).                                   |
| `PATH_PREFIX`                       |         | No       | Sub-path where the app is mounted (e.g., `/drop` if available at `example.org/drop`). Leave empty for root. |
| `FILE_KEEP_TIME_IN_HOURS`           | `24`    | No       | Hours to keep encrypted drops before automatic deletion.                                                    |
| `SHOW_GITHUB_LINK_IN_FOOTER`        | `true`  | No       | Show GitHub repository link in the footer.                                                                  |
| `SITE_PRIVACY_POLICY`               |         | No       | URL to your privacy policy. Leave empty to hide the privacy policy link.                                    |
| `SHOW_LANGUAGE_SELECTION_IN_FOOTER` | `true`  | No       | Show language selection dropdown in the footer.                                                             |
| `SHYNET_HOST`                       |         | No       | Hostname of your Shynet analytics instance (requires `SHYNET_ID` to be active).                             |
| `SHYNET_ID`                         |         | No       | Site ID on Shynet (requires `SHYNET_HOST` to be active).                                                    |
| `DO_NOT_TRACK`                      | `false` | No       | Disable Shynet tracking (only effective if both `SHYNET_HOST` and `SHYNET_ID` are set).                     |

### Data Persistence

By default, drops are stored in the container and lost when it restarts. To persist encrypted drops across container
restarts, mount these volumes:

| Mount Point                     | Purpose                                                                                                                                                                            |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/var/dead-drop/data`           | Encrypted drop storage. Drops are automatically deleted after `FILE_KEEP_TIME_IN_HOURS`.                                                                                           |
| `/var/dead-drop/key/key.secret` | Server-side encryption key. Required to decrypt persisted drops. Keep this safe! (Feel free to just mount the folder `/var/dead-drop/key`, so that the file can be created in it.) |

Mount both volumes to a host directory (as shown in the Quick Start example) to enable persistence.

## Acknowledgments

This project is heavily inspired by [BillKeenan/dead-drop-python](https://github.com/BillKeenan/dead-drop-python), which
I used (self-hosted) before creating this service.

# LICENSE

```
Copyright 2026 Florian Mötz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
