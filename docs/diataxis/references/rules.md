# Rules XML Reference

## General Structure
Each file should contain only 1 `RuleImport`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<RuleImport>
  <RuleAssignment>
    <Target Context="OC_RULES_V1">SE_OID.CRF_OID.IG_OID.ITEM_OID</Target>
    <RuleRef OID="RULE_1">
      <DiscrepancyNoteAction IfExpressionEvaluates="true">
        <Run InitialDataEntry="true"/>
        <Message>Value is out of range</Message>
      </DiscrepancyNoteAction>
    </RuleRef>
  </RuleAssignment>
  <RuleDef OID="RULE_1" Name="Age Check">
    <Description>Checks if age is valid</Description>
    <Expression>SE_OID.CRF_OID.IG_OID.ITEM_OID gt 18</Expression>
  </RuleDef>
</RuleImport>
```

## `RuleAssignment`
Rule Assignment contains one target with one or more `RuleRef`s. This is where you define the variable to which the Rule applies, when the Rule should be executed, and the action(s) that should occur.

```xml
<RuleAssignment>
  <Target Context="OC_RULES_V1">SE_OID.CRF_OID.IG_OID.ITEM_OID</Target>
  <RuleRef OID="RULE_2">
    <ShowAction IfExpressionEvaluates="true">
      <Run InitialDataEntry="true"/>
      <Message>Showing field</Message>
      <DestinationProperty OID="ITEM_OID_2"/>
    </ShowAction>
  </RuleRef>
</RuleAssignment>
```

## Actions

### `DiscrepancyNoteAction`
Creates a Discrepancy Note automatically when the RuleDef Expression evaluates to the result indicated.

```xml
<DiscrepancyNoteAction IfExpressionEvaluates="true">
  <Run InitialDataEntry="true"/>
  <Message>Discrepancy Note</Message>
</DiscrepancyNoteAction>
```

### `EmailAction`
Sends an email automatically when the RuleDef expression evaluates to the result indicated. The `To` tag allows you to specify an email account to receive the email.

```xml
<EmailAction IfExpressionEvaluates="true">
  <Run InitialDataEntry="true"/>
  <Message>Email Subject and Body</Message>
  <To>admin@example.com</To>
</EmailAction>
```

### `InsertAction`
Used to insert a value into another item in the same CRF or into another CRF in the same event definition. `DestinationProperty` is the item that will get the value inserted.

```xml
<InsertAction IfExpressionEvaluates="true">
  <Run InitialDataEntry="true"/>
  <DestinationProperty OID="ITEM_OID_3">
    <ValueExpression Context="OC_RULES_V1">"New Value"</ValueExpression>
  </DestinationProperty>
</InsertAction>
```

### `RandomizeAction`
Similar to InsertAction, used for randomization.

```xml
<RandomizeAction IfExpressionEvaluates="true">
  <Run InitialDataEntry="true"/>
  <DestinationProperty OID="ITEM_OID_4"/>
</RandomizeAction>
```

### `NotificationAction`
Used to send an SMS and/or email notification to a Study Participant, or an email notification to any specified email address.

```xml
<NotificationAction IfExpressionEvaluates="true">
  <To>participant@example.com</To>
  <Subject>Notification</Subject>
  <Message>This is a notification</Message>
</NotificationAction>
```

### `ShowAction` / `HideAction`
Displays or hides an Item or Group automatically when the referenced RuleDef Expression evaluates to the value specified.

```xml
<HideAction IfExpressionEvaluates="true">
  <Run InitialDataEntry="true"/>
  <Message>Hiding field</Message>
  <DestinationProperty OID="ITEM_OID_5"/>
</HideAction>
```

### `EventAction`
Used to schedule an event automatically based on the Status or StartDate of another Event.

```xml
<EventAction IfExpressionEvaluates="true">
  <EventDestination Property="StartDate">
    <ValueExpression Context="OC_RULES_V1">"2023-01-01"</ValueExpression>
  </EventDestination>
</EventAction>
```

## `RuleDef`
Where you define your expression and Rule description. OID must be unique, all UPPERCASE, and cannot contain whitespace. `Expression` is the mathematical definition of what is being evaluated.

```xml
<RuleDef OID="RULE_EXAMPLE" Name="Example Rule">
  <Description>Description of the rule</Description>
  <Expression>1 eq 1</Expression>
</RuleDef>
```
