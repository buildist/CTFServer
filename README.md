A Minecraft Classic server software made by Jacob_ based upon OpenCraft 0.2.

# Issue Reporting
Please tag your issue appropriately and give as much information as possible so that dev's will have an easier
time debugging and fixing the issue. 


# Contributing Guidelines
1. Please make an issue if there is not already one in
2. When working on an issue, please assign yourself so that others know not to work on it.
3. Don't take other peoples issues without permission (unless it's been a long time and nothing has been done on it)
4. Please create a separate branch (off of master), and merge request for that issue when working on it. 
That way the master branch is always in a working state. You can use the branch/merge request creation tool
that is on an issue. In fact, please do, this keeps branch names consistent, and will set things up properly so that
when the branch is merged, the issue is also automatically closed.
5. Merge your branch into master through merge requests (even if you are a maintainer). This helps keep things 
consistent and helps keep track of what commits go with what issue.


# Setting up the Development Environment
## IntelliJ
### Importing the project
When getting setup with IntelliJ, just clone the repository and import the project.
IntelliJ should automatically detect your java version, and the libraries
that are needed to import the project.
### Adding a configuration
After importing the project, you will need to add a configuration in IntelliJ. 
- Click on Add Configuration in the top right
- Click the plus button in the top left, and select the type of application
- Give it a name
- Set the main class to `org.opencraft.server.Server`
- Done! Test the configurations by running it.