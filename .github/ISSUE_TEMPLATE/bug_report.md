---
name: Bug report
about: "You're having technical issues. \U0001F41E"
labels: "bug"
body:
  - type: markdown
    attributes:
      value: |
        Thank you for helping us in making dt-tool better!

        **Please do not ask questions here!**
  - type: textarea
    id: behavior-current
    attributes:
      label: Current Behavior
      description: |-
        Describe the current faulty behavior that you observed.
        Consider providing screenshots, log output, and other supplementary data.

        *Files and images can be included via drag and drop into this text field.*
    validations:
      required: true
  - type: textarea
    id: steps-to-reproduce
    attributes:
      label: Steps to Reproduce
      description: |-
        Describe the exact steps of how the defect can be reproduced.
        Consider providing screenshots, BOM files, and other supplementary data.

        *Files and images can be included via drag and drop into this text field.      
      value: |-
        1.
    validations:
      required: true
  - type: textarea
    id: behavior-expected
    attributes:
      label: Expected Behavior
      description: >-
        Describe how you expect dt-tool to behave instead.
    validations:
      required: true
  - type: markdown
    attributes:
      value: |
        ## Environment
        Please provide some details about the environment in which you observed the defect.
        - Java version :
        - Java distribution:
        - Operating System and version :
  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist
      options:
        - label: I have checked the [existing issues](https://github.com/elomagic/dt-tool/issues) for whether this defect was already reported
          required: true
