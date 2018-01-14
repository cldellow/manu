# Maintainers & Contributors

* Colin Dellow

Feedback, issue reports, PRs, etc, welcome.

## Doing a release to Maven Central

1. Configure your `~/.m2/settings.xml`:

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>jira-username</username>
      <password>jira-password</password>
    </server>
  </servers>
</settings>
```

2. Create a `gpg` key

See http://central.sonatype.org/pages/working-with-pgp-signatures.html

3. Publish to Sonatype

Set a non SNAPSHOT version.

```
mvn versions:set -DnewVersion=x.y.z
RELEASE=1 mvn clean deploy
```

4. Validate

The build should flow to Maven Central automatically.

Inspect it at:

- https://oss.sonatype.org/
- https://oss.sonatype.org/content/repositories/snapshots/com/cldellow/

See also:

- http://central.sonatype.org/pages/apache-maven.html
- http://central.sonatype.org/pages/ossrh-guide.html
