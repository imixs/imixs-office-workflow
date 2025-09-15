# Dashboard

The Dashboard feature gives you a great opportunity to display open task, stats and important information to users or user groups.
Dashboards can be configured individual simmilar like custom forms.

<img class="screenshot" src="dashboard-001.png" />

## Setup & Layout

To define a new dashboard you simply add a dashboard definition into the layout section of the general configuraiton:

```xml
<imixs-form>
  <imixs-form-section columns="4" label="">
     <item name="dashboard.worklist.count.today" type="custom" path="cards/plain"  label=""
           options='{"class":"flat success", "icon":"fa-inbox", "label":"Neue Aufgaben", "description":"Neue Aufgaben seit Heute"}'   />
     <item name="dashboard.worklist.count.oneweek" type="custom" path="cards/plain"  label=""
           options='{"class":"flat warning", "icon":"fa-exclamation-triangle", "label":"Zu Beachten", "description":"Aufgaben seit einer Woche offen"}'    />
     <item name="dashboard.worklist.count.urgent" type="custom" path="cards/plain"  label=""
           options='{"class":"flat error", "icon":"fa-fire", "label":"Dringend", "description":"Aufgaben seit mehr als 1 Woche offen"}'    />
     <item name="dashboard.worklist.count.all" type="custom" path="cards/plain" label=""
           options='{"class":"flat", "icon":"fa-tasks", "label":"Alle Aufgaben", "description":"Meine offenen Aufgaben"}' />
   </imixs-form-section>
   <imixs-form-section columns="2">
     <item name="dashboard.worklist.owner" type="custom" path="cards/worklist"
         options='{ "label":"Meine Aufgaben", "description":"Aufgaben die offen sind"}'/>
     <item name="dashboard.worklist.creator" type="custom" path="cards/worklist"
         options='{ "label":"Meine Vorgänge", "description":""}'/>
   </imixs-form-section>
   <imixs-form-section columns="3" label="">
      <item name="worklist.stats.count.beschaffung" type="custom" path="cards/plain"
            options='{"class":"lead", "key":"process", "value":"Beschaffung"}'   />
      <item name="worklist.stats.chart.beschaffung" type="custom" path="cards/chart"
            options='{"key":"process", "value":"Beschaffung"}'   />
      <item name="worklist.stats.chart.rechnungseingang" type="custom" path="cards/chart"
            options='{"key":"$workflowgroup", "value":"Rechnungseingang"}'   />
   </imixs-form-section>
</imixs-form>
```

See the [layout section](./layout.html) for more information about how to arrange elements.

### Cards

Da dashboard consists on form card elements located under `/cards`

Each card can display information provided by the `DashboardAnalyticController` component. The following table shows an overview about predefined card content:

| Analytic ID                       | Type    | Card         | Description                                                                     |
| --------------------------------- | ------- | ------------ | ------------------------------------------------------------------------------- |
| **Counter**                       |         |              |                                                                                 |
| dashboard.worklist.count.today    | counter | plain        | Counts all new tasks for the current user for this day                          |
| dashboard.worklist.count.thisweek | counter | plain        | Counts all tasks for the current user since the current week (stared on Monday) |
| dashboard.worklist.count.oneweek  | counter | plain        | Counts all tasks for the current user since the last 7 days                     |
| dashboard.worklist.count.urgent   | counter | plain        | Counts all tasks for the current user older then 7 days                         |
| dashboard.worklist.count.all      | counter | plain        | Counts all tasks for the current user                                           |
| contract.name                     | text    | plain        | Contract name                                                                   |
| contract.partner                  | text    | plain        | Contract partner name                                                           |
| contract.number                   | text    | plain        | Contract number                                                                 |
| contract.start                    | date    | plain        | Contract start date                                                             |
| **Views**                         |         |              |                                                                                 |
| dashboard.worklist.owner          | view    | worklist     | Shows a view with all tasks for the current user                                |
| dashboard.worklist.creator        | view    | worklist     | Shows a view with all tasks created by the current user                         |
| dashboard.worklist.participant    | view    | worklist     | Shows a view with all tasks the current user is a participant                   |
| dashboard.worklist.favorite       | view    | worklist     | Shows a view with all favorites tasks marked by the current user                |
| **Information**                   |         |              |                                                                                 |
| [PROCESS NAME]                    | counter | startprocess | Shows information about a process                                               |
