# manu-report

A shim to split the aggregate report that Jacoco's maven plugin produces into N
reports, 1 for each project. This is needed for Codecov to parse it correctly.

It's likely I'm missing something and it should be possible to have Jacoco
directly emit 1 report per project.

*Update after futzing*: There is a very good chance that a combination of Codecov's
caching plus their default view being minimum coverage per day meant that I inadvertently
missed an easier way to do this. Still, this is good enough for now.
