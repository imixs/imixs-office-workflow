# Dashboard

The Dashboard feature gives you a great opportunity to display open task, stats and important information to users or user groups.
Dashboards can be configured individual simmilar like custom forms.

<img class="screenshot" src="dashboard-001.png" />

## Setup & Layout

To define a new dashboard you simply add a dashboard definition into the layout section of the general configuraiton:

```xml
<imixs-form>
<imixs-form-section columns="4" label="My Dashboard">
     <item name="dashboard.worklist.count.today" type="custom" path="cards/plain"  label=""
           options='{"class":"flat info", "icon":"fa-inbox", "label":"Neue Aufgaben", "description":"Neue Aufgaben seit Heute"}'   />
     <item name="dashboard.worklist.count.thisweek" type="custom" path="cards/plain"  label=""
           options='{"class":"flat info", "icon":"fa-inbox", "label":"Neue Aufgaben", "description":"Neue Aufgaben seit Montag"}'   />
     <item name="dashboard.worklist.count.oneweek" type="custom" path="cards/plain"  label=""
           options='{"class":"flat warning", "icon":"fa-exclamation-triangle", "label":"Zu Beachten", "description":"Aufgaben seit einer Woche offen"}'    />
     <item name="dashboard.worklist.count.urgent" type="custom" path="cards/plain"  label=""
           options='{"class":"flat error", "icon":"fa-fire", "label":"Dringend", "description":"Aufgaben seit mehr als 1 Woche offen"}'    />
   </imixs-form-section>

  <imixs-form-section columns="2">
   <item name="dashboard.worklist.owner" type="custom" path="cards/worklist"
         options='{ "label":"Meine Aufgaben", "description":"Aufgaben die offen sind"}'/>
   <item name="dashboard.worklist.creator" type="custom" path="cards/worklist"
         options='{ "label":"Meine Vorgänge", "description":""}'/>
  </imixs-form-section>

  <imixs-form-section columns="4" label="">
     <item name="Beschaffung" type="custom" path="cards/startprocess"
           options='{"class":"small", "icon":"fa-inbox"}'   />
     <item name="Controlling" type="custom" path="cards/startprocess"
           options='{"class":"small", "icon":"fa-exclamation-triangle"}'  />
     <item name="Empfang" type="custom" path="cards/startprocess"
           options='{"class":"small", "icon":"fa-fire" }'    />
     <item name="Vertrieb" type="custom" path="cards/startprocess"
           options='{"class":"small", "icon":"fa-tasks"}' />
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
| dashboard.worklist.favorite       | view    | worklist     | Shows a view with all favorites tasks marked by the current user                |
| **Information**                   |         |              |                                                                                 |
| [PROCESS NAME]                    | counter | startprocess | Shows information about a process                                               |
