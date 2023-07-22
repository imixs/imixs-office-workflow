# Prozessbasiertes ERP

Process-Based-ERP ist ein Ansatz Unternehmensabläufe als Geschäftsprozesse zu verstehen und zu modellieren, anstatt diese wie bisher als rein statische Datenmodelle abzubilden.

Klassische ERP Systeme basieren auf dem Ansatz sämtliche Geschäftsprozesse ausschließlich als Datenobjekte zu modellieren. Beispielsweise wird eine Rechnung in einen Rechnungskopf und eine Reihe von Rechnungspositionen modelliert und in zwei getrennten Datentabellen mit entsprechenden Relationen abgebildet.
Die Datenbanktabellen nehmen dabei alle Eigenschaften auf, die einer Rechnung zugeschrieben werden. Beispielsweise die Rechnungsnummer, das Datum den Rechnungsbetrag sowie einzelne Rechnungspositionen mit Artikelbezeichnung, Menge, Einzelpreis und Gesamtpreis.
Diese Art von Datenmodellierung basiert auf der Annahme das die Daten eine statische unveränderliche Struktur aufweisen - Es ergibt sich so das sogenannte DatenbankSchema.

BILD

    INVOICE_HEAD    INVOICE_POS    PAYMENT

Bei Process Based ERP werden statt der Datenmodelle ausschließlich die tatsächlichen Geschäftsprozesse modelliert. Es müssen also keine Datentabellen für die einzelnen Geschäftsobjekte angelegt werden, sondern die Geschäftsprozesse selbst beschrieben und modelliert.

Für eine Rechnung bedeutet dies beispielsweise:

- Die Erstellung der Rechnung
- Die Rechnungsprüfung und Freigabe
- Der Rechnungsversand
- Die Überwachung des Zahlungsziels
- Die Verbuchung der Rechnung
- Ggf. ein Mahnlauf

Bild

```
   Create Invoice -> Approve -> Send Invoice  -> Booking ->  Reminder  |-> Payment
                                                                       |
                                                                       |-> Dunning
```

Was bedeutet dies nun für die Praxis? In der modern Geschäftswelt von heute ändern sich Unternehmensabläufe sehr schnell. Sie unterliegen permanenten Neuanforderungen von Seiten des Gesetzgebers, dem Geschäftspartner oder der eigenen Unternehmensorganisation. Die klassische Modellierung statischer Datenmodelle kommt hier schnell an Ihre Grenzen, da nachträgliche Änderungen an einem Datenmodell meist sehr Aufwändig und damit Kostenintensiv sind.

Wird beispielsweise im zuvor beschriebenen Rechnungsprozess eine zusätzliche Referenznummer benötigt - z.B. die Referenz auf einen neuen Online Shop - bekommen neue Rechnungen einen entsprechenden Eintrag in die nun erweiterte Datentabelle. Alte Rechnungen führen an dieser Stelle nun eine unnötige Leerspalte mit. Handelt es sich bei der Referenz um ein verpflichtende Angabe, weisen alte Rechnungen nun eine Lücke auf - das Datenmodell wird inkonsistent. Und nur mit dem fachliche Hintergrundwissen über die Änderung, kann einen Datensatz künftig richtig interpretiert werden.

Die Folge ist, dass ERP Systeme meist nach einigen Jahren sehr komplex und unhandlich werden. Auf Änderungen kann mit immer nur noch größerem Aufwand reagiert werden. Der Kauf einer neuen ERP Lösung scheint dann oft der einzige Ausweg zu sein. Und in der Tat lassen sich die Anforderungen scheinbar auf der neuen Plattform viel schneller und besser abbilden. Der Grund hierfür ist aber lediglich die Tatsache, das ein neues geändertes Wissen über die benötigen Strukturen vorliegt. Das eigentliche Problem dynamischer Geschäftsprozesse wurde nur vorübergehend durch die Einführung einer neuen Lösung verdeckt.

(Praktisch geht es um ein Problem das erst in den letzten 10 Jahren wirklich deutlich zu Tage tritt - nachdem das 2. oder 3. ERP System eingeführt wurde)

## Process Based ERP

Hier kann nun der Process-Based ERP Ansatz eine völlig neue Erfahrung darstellen.
