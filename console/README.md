# RMAC Console

<p align="center">
<a href="https://github.com/saurabh-prosoft/rmac/actions/workflows/build-console-deploy-bridge.yml">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/saurabh-prosoft/rmac/build-console-deploy-bridge.yml?branch=main&label=Build%20%26%20Deploy&logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABGdBTUEAALGPC%2FxhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t%2FAAAACXBIWXMAAABgAAAAYADwa0LPAAAAB3RJTUUH5gUKFyETutzc4wAABApJREFUSMedlH1M1WUUxz%2Fnd2HGVsyLeMk%2F2HRo%2BgerSeIE3eRNJoo01Li6xaw1UAspV7zNF0Rteq9by%2BlUslUiMzEvKPEiV2DpFJZbOcNcGBW5ZclVzIGIee%2Fv9Ede2rjdXev717Nn55zP95zn2RHGqazMtV21qYk2%2BQjS02nTU9DdbZi%2BPigq2jUlL0%2Fk2jX%2Bo8oW1TtVV63CrfGwezefyh%2BwZ4%2BMD6xIa5ipmpTkazF9cOaMVOKAyEiW4oGBAWOGz4rGx%2B%2BakmcXw%2BMJBnxLG1R14sSnEsxXUZeLTHKQtLSxgNucgP5%2Bo%2FQb14hqc3PFFtf7qvPn7%2BrM7RXp7qaATLSoaCyhmclgs%2Fm%2BCvsFVq8O1XF4%2FqPPwWqlHjuycGFAwA15ESoqDHmJLZCSYj7NILS2lpe74lULC6nmPCxbNj5PjukpJCYmGPjdV%2BqdatpshtNyFRobOcQDsFhok40wOoqbRrSz03F2eanI8eNheFmHdnUxyLdIRobCbKiulipA%2FoUwTXrBag0O1mGko0P28gLEx%2FMsL6OtrUaUzEDy88053iXg9XKZIwCG7pZRpLiYJj6AW7dCfyf9ENavL%2Fv5ZKPq1q2gqipiydSpcPq0H6x2sqClZWRmxPeQm%2Fv3096543Dm2cW4d29sov7DOwlfJKgZHW2Z%2FucpJC9P6ogFm00iCQerVd%2BgDoqLA%2Fys03CorDQ%2Blj7o6PDlMws2bHjQFzEdXbNm35Ilz4nx8GGwdoQnVLn95HU1S0p0qvQgTmfAXEpxwbZtzkkrjolUVT1p3ZAGSnc2rFVz7lzLQ3MmEh6u1zUbTU4OZsQ%2FEce0lTki27f%2FbwMl5%2BsnqSYkGHF6Fc6epVnS0AkTKPBdQ7Kz6TTeRBcswE06smNHQOFGKYf9%2BxnRGXD3rllAJzowYHnddwTq6vx7JMCAv2MZMksRt5tZHISbN3UpI%2BjFi%2FIJUYjdbgwTBVlZZgp9aGpqMCMB%2BmehRUFqqhEK7PUxgmZlSTuVSGwsg7wGpumbYPSijx450lccEGPnTi5wC%2FbtC2ng8UIzEyyz0b17jfIvXTfUTEwMBg4b4hAcPMgVhmHePH3GcKKZmc7NudViXLo0VvgC62FwMAD4q3wGtbXUyVy0sJAoLsPQEFWSA0lJhuYyDDU1lLMFIiMlw5eI5uSMgX%2FnByQ5OSj4sfQ9ItDbtwPuRW2o2%2B3oXx4rxuHD1OgxWLxY5%2BjXyLlzUtbjmqOmx0MtFUh0NHGyGb1%2Fnx%2B1CfF6Q4H9qvjtRJ2akyebGZb9SE8P2bwNMTFjHT8GO75b%2BZNIV5c%2Fz5ArrEWKiuhmEXg83NM2pL9fG3QjmpERCuyX%2F1drqRyA1FRqeB5tbyeOZRAWpkflKmzaND7vL7TQ7bt9WJJoAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDIyLTA1LTEwVDIzOjMzOjE5KzAwOjAw7KPZpwAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyMi0wNS0xMFQyMzozMzoxOSswMDowMJ3%2BYRsAAAAASUVORK5CYII%3D&style=flat-square">
</a>
<a href="https://app.codecov.io/gh/saurabh-prosoft/rmac">
<img alt="Codecov test coverage" src="https://img.shields.io/codecov/c/gh/saurabh-prosoft/rmac/main?flag=console&label=Coverage&logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABGdBTUEAALGPC%2FxhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t%2FAAAACXBIWXMAAABgAAAAYADwa0LPAAAAB3RJTUUH5gUKFzMgffjNJgAAAj1JREFUSMfVkk9I03EYxp%2F3pxuixyCpgSRDJylUGAuiwuZB5mEXMRXMAmtEsEAJzJ8LMWTuYEEXD0rKJBFyTOniQYLC44QMm8SiUEL0Yv%2FY5rbY7%2BmSc6jjF20eeo%2Ff7%2FO%2Bz%2Bd9vl%2BBTlGj1k%2BjERJb%2FLUeiQA4j1GDIXsDAmgJBEQpue4509SkN1%2FRE4giyoAkk6DM4dbwcEarVUytrQAW%2BGhkBMA82hMJQF5q37xevbl%2FDbBHgrN8uLS0d1D0Pvza7wfwXALBIIA13E2lRCme8dYFg%2FkHOKL6rwAScsNkQvqtgdMvSED5jsF4HEAdzEYjtcgn9V5pad4AqFFz3jYYABbC1NUFoBIXJyZERAZE04CiBcO7uTkAgp%2BbmxCZZa%2FbnbeIyOh0r7ezk4y%2BUdeSSTJ%2B2V1WXn6ILqomnc4MXcDdYjbnYEw2NxcUkFGXeiocphY7rl4YG9PXx1xq4eoqGZtVn05N6flI9uijJ9XHHR0QfMS2zwegD1cWF0EU8urWVvaJsi3PqqsB9uNDVRWA%2B7hUWytS4vdcW17WBdjdBNgZrHgVCgHsxh2L5d%2BjlHPYmJ8XpTjs8TU27r8%2B%2BAkZ%2FVxxoq0tZ%2BP0inwLk91ORtrdZTZbVgCS7KeiQBQXTD09ORsfJOnWKoeGSBKQdPIZCew4k6MOB8AZ%2FKipyT8ALLBarUCs%2BcGXhoZDAGiBo77%2BCIz31zH5arcfAgDQNj4OiJ2ulRUA6%2BhKpfJg%2BGeO3GRfKATgCacnJ3cvfwN9%2FfvYsMKoQwAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAyMi0wNS0xMFQyMzo1MTozMiswMDowMEDBkF4AAAAldEVYdGRhdGU6bW9kaWZ5ADIwMjItMDUtMTBUMjM6NTE6MzIrMDA6MDAxnCjiAAAAAElFTkSuQmCC&style=flat-square">
</a>
<a href="https://sonarcloud.io/summary/new_code?id=saurabh-prosoft_rmac_console">
<img src="https://sonarcloud.io/api/project_badges/measure?project=saurabh-prosoft_rmac_console&metric=alert_status" />
</a>
</p>

<br/>

RMAC Console is a centralized dashboard that provides a user-friendly way to manage, monitor and interact with the host machines running RMAC Host-Clients.

View the publicy available RMAC Console [here](https://console.rmac.saurabhagat.me) ! 

## Features

View all the hosts that are online as well as hosts that are registered but offline

<p align="center">
<img src="https://raw.githubusercontent.com/saurabh-prosoft/saurabh-prosoft.github.io/readme-resources/rmac/rmac-console-demo-1.gif" />
</p>

<br/>

View and edit configuration and properties of an online host

<p align="center">
<img src="https://raw.githubusercontent.com/saurabh-prosoft/saurabh-prosoft.github.io/readme-resources/rmac/rmac-console-demo-2.gif" />
</p>

<br/>

Open an interactive shell and run commands on the host directly

<p align="center">
<img src="https://raw.githubusercontent.com/saurabh-prosoft/saurabh-prosoft.github.io/readme-resources/rmac/rmac-console-demo-3.gif" />
</p>

and much more !
