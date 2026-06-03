
<div id="top"></div>

<!-- PROJECT SHIELDS -->
![GitHub last commit](https://img.shields.io/github/last-commit/thehackermonk/boooks) ![GitHub](https://img.shields.io/github/license/thehackermonk/boooks?) ![GitHub issues](https://img.shields.io/github/issues/thehackermonk/boooks) ![GitHub language count](https://img.shields.io/github/languages/count/thehackermonk/boooks) ![GitHub top language](https://img.shields.io/github/languages/top/thehackermonk/boooks) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/thehackermonk/boooks) ![Lines of code](https://img.shields.io/tokei/lines/github/thehackermonk/boooks) ![Twitter Follow](https://img.shields.io/twitter/follow/thehackermonk)


<div align="center">
<h3 align="center">Boooks</h3>
  <p align="center">
    Boooks is a library management system that is designed to manage all the functions of a library.
    <br />
    <a href="https://github.com/thehackermonk/boooks"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/thehackermonk/boooks">View Demo</a>
    ·
    <a href="https://github.com/thehackermonk/boooks/issues">Report Bug</a>
    ·
    <a href="https://github.com/thehackermonk/boooks/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#features">Features</a></li>
    <li><a href="#installation">Installation</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#api-endpoints">API Endpoints</a></li>
    <li><a href="#monitoring-and-management">Monitoring and Management</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#authors">Authors</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

Boooks is a library management system that is designed to manage all the functions of a library. It helps the librarian to maintain the database of new books and the books that are borrowed by the members along with their due dates.

This system completely automates all your library's activities. The best way to maintain, organize and handle countless books systematically is to implement a library management system software.

You can find books in an instant, issue/reissue books quickly, and manage all the data efficiently and orderly using this system. The purpose of a library management system is to provide instant and accurate data regarding any type of book, thereby saving a lot of time and effort.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- FEATURES -->
## Features

- Authentication: Authentication includes registration of new librarians, allowing them to log in and log out of the system. It also provides functionality for librarians to change their password and recover forgotten passwords.
- Librarian management: Librarian management allows the retrieval of details for all librarians. It provides the ability to activate or inactivate librarians and also supports the deletion of inactivated librarians.
- Author management: Author management enables users to view all authors and browse all books written by a specific author.
- Book management: Book management facilitates the retrieval of details for all books in the library. It allows users to obtain information about a specific book and provides functionality to add, update, and delete books in the library.
- Fine management: Fine management includes the retrieval of fine details. It allows the configuration of the number of days after which fines should be collected and the amount to be charged per day as a fine.
- Book issuing and returning: Book issuing and returning functionality enables librarians to issue books to library members and manage the return of books to the library.
- Log management: Log management provides the ability to maintain and view the library log.
- Member management: Member management allows the retrieval of details for all library members. It supports obtaining information about a specific member and provides functionality to add, update, and delete library members. It also tracks the books currently held by each member.
- Search functionality: Search functionality allows users to search for books based on title and author. It also enables the search for members based on their name, email, and phone number.
- Security: Security measures include the implementation of authentication tokens for API security and the use of refresh tokens for session security.
- Swagger API documentation: Access detailed documentation of the available APIs using Swagger.
- Actuator: Monitor and manage the application using Actuator endpoints.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- INSTALLATION -->
## Installation

1. Clone the repository: `git clone` [thehackermonk/Boooks](https://github.com/thehackermonk/Boooks)
2. Navigate to the project directory: `cd boooks`
3. Build the project: `mvn clean install`
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE -->
## Usage

1. Start the application: `mvn spring-boot:run` or run the `BoooksApplication` class.
2. Access the application in your browser: `http://localhost:8080`
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- API ENDPOINTS -->
## API Endpoints

- View API documentation: [Swagger UI](http://localhost:8080/swagger-ui/index.html#/)
- List of available endpoints, request/response formats, and authentication requirements can be found in the Swagger documentation.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- MONITORING AND MANAGEMENT -->
## Monitoring and Management

- [Expose available caches: /actuator/caches](http://localhost:8080/actuator/caches)
- [Show the conditions that were evaluated on configuration and auto-configuration classes and the reasons why they did or did not match: /conditions](http://localhost:8080/actuator/conditions)
- [Display a collated list of all @ConfigurationProperties: /actuator/configprops](http://localhost:8080/actuator/configprops)
- [Expose properties from Spring's ConfigurableEnvironment: /actuator/env](http://localhost:8080/actuator/env)
- [Check the application health: /actuator/health](http://localhost:8080/actuator/health)
- [Display application information: /actuator/info](http://localhost:8080/actuator/info)
- [Show 'metrics' information for the current application: /actuator/metrics](http://localhost:8080/actuator/metrics)
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## Contributing

Contributions are welcome! If you'd like to contribute to Boooks, please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them.
4. Push your changes to your forked repository.
5. Submit a pull request describing your changes.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- LICENSE -->
## License

This project is licensed under the [GNU General Public License v3.0](https://opensource.org/license/gpl-3-0/).
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- AUTHORS -->
## Authors

Karthik Prakash - [@thehackermonk](https://github.com/thehackermonk)
<p align="right">(<a href="#top">back to top</a>)</p>
