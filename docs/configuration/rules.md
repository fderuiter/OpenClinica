# Rules XML Reference

## General Structure
Each file should contain only 1 `RuleImport`.

## `RuleAssignment`
Rule Assignment contains one target with one or more `RuleRef`s. This is where you define the variable to which the Rule applies, when the Rule should be executed, and the action(s) that should occur.

### `Target`
Rules are assigned to a particular data item in a CRF via the Target. This defines which variable will display any associated error message. Within the Target, the Context should always be `Context="OC_RULES_V1"`.

### `RunOnSchedule`
OpenClinica can run rules automatically at any hour, on the hour. Time must be entered in 24-hour format (00:00 to 23:59). In the `Run` tag, you must set `Batch="true"`.

### `RuleRef`
RuleRef is where you reference an existing Rule OID from the RuleDef section below. The RuleRef OID can only contain capital letters, numbers, and underscores (no spaces).

## Actions

### `DiscrepancyNoteAction`
Creates a Discrepancy Note automatically when the RuleDef Expression evaluates to the result indicated.

### `EmailAction`
Sends an email automatically when the RuleDef expression evaluates to the result indicated. The `To` tag allows you to specify an email account to receive the email.

### `InsertAction`
Used to insert a value into another item in the same CRF or into another CRF in the same event definition. `DestinationProperty` is the item that will get the value inserted.

### `RandomizeAction`
Similar to InsertAction, used for randomization.

### `NotificationAction`
Used to send an SMS and/or email notification to a Study Participant, or an email notification to any specified email address.

### `ShowAction` / `HideAction`
Displays or hides an Item or Group automatically when the referenced RuleDef Expression evaluates to the value specified.

### `EventAction`
Used to schedule an event automatically based on the Status or StartDate of another Event.

## `RuleDef`
Where you define your expression and Rule description. OID must be unique, all UPPERCASE, and cannot contain whitespace. `Expression` is the mathematical definition of what is being evaluated.
