package de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.vormerken;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uni_hamburg.informatik.swt.se2.mediathek.fachwerte.Datum;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.Kunde;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.medien.Medium;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.ServiceObserver;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.kundenstamm.KundenstammService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.medienbestand.MedienbestandService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.verleih.ProtokollierException;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.verleih.VerleihService;
import de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.SubWerkzeugObserver;
import de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.subwerkzeuge.kundenauflister.KundenauflisterWerkzeug;
import de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.subwerkzeuge.kundendetailanzeiger.KundenDetailAnzeigerWerkzeug;
import de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.subwerkzeuge.mediendetailanzeiger.MedienDetailAnzeigerWerkzeug;
import de.uni_hamburg.informatik.swt.se2.mediathek.werkzeuge.subwerkzeuge.vormerkmedienauflister.VormerkMedienauflisterWerkzeug;

/**
 * Ein VormerkWerkzeug stellt die Funktionalität des Vormerkens für die
 * Benutzungsoberfläche bereit. Die UI wird durch die VormerkUI gestaltet.
 * 
 * @author SE2-Team
 * @version SoSe 2021
 */
 public class VormerkWerkzeug
{

    /**
     * Die UI-Komponente der Ausleihe.
     */
    private final VormerkUI _vormerkUI;

    /**
     * Der Service zum Ausleihen von Medien.
     */
    private final VerleihService _verleihService;

    /**
     * Das Sub-Werkzeug zum Darstellen und Selektieren der Kunden.
     */
    private KundenauflisterWerkzeug _kundenAuflisterWerkzeug;

    /**
     * Das Sub-Werkzeug zum Darstellen und Selektieren der Medien.
     */
    private VormerkMedienauflisterWerkzeug _medienAuflisterWerkzeug;

    /**
     * Das Sub-Werkzeug zum Anzeigen der Details der selektieten Medien.
     */
    private MedienDetailAnzeigerWerkzeug _medienDetailAnzeigerWerkzeug;

    /**
     * Das Sub-Werkzeug zum Anzeigen der Details des selektieten Kunden.
     */
    private KundenDetailAnzeigerWerkzeug _kundenDetailAnzeigerWerkzeug;

    /**
     * Initialisiert ein neues VormerkWerkzeug. Es wird die Benutzungsoberfläche
     * mit den Ausleihaktionen erzeugt, Beobachter an den Services registriert
     * und die anzuzeigenden Materialien gesetzt.
     * 
     * @param medienbestand Der Medienbestand.
     * @param kundenstamm Der Kundenstamm.
     * @param verleihService Der Verleih-Service.
     * 
     * @require medienbestand != null
     * @require kundenstamm != null
     * @require verleihService != null
     */
    public VormerkWerkzeug(MedienbestandService medienbestand,
            KundenstammService kundenstamm, VerleihService verleihService)
    {
        assert medienbestand != null : "Vorbedingung verletzt: medienbestand != null";
        assert kundenstamm != null : "Vorbedingung verletzt: kundenstamm != null";
        assert verleihService != null : "Vorbedingung verletzt: verleihService != null";

        _verleihService = verleihService;

        // Subwerkzeuge erstellen
        _kundenAuflisterWerkzeug = new KundenauflisterWerkzeug(kundenstamm);
        _medienAuflisterWerkzeug = new VormerkMedienauflisterWerkzeug(
                medienbestand, verleihService);
        _medienDetailAnzeigerWerkzeug = new MedienDetailAnzeigerWerkzeug();
        _kundenDetailAnzeigerWerkzeug = new KundenDetailAnzeigerWerkzeug();

        // UI wird erzeugt.
        _vormerkUI = new VormerkUI(_kundenAuflisterWerkzeug.getUIPanel(),
                _medienAuflisterWerkzeug.getUIPanel(),
                _kundenDetailAnzeigerWerkzeug.getUIPanel(),
                _medienDetailAnzeigerWerkzeug.getUIPanel());

        // Beobachter erzeugen und an den Services registrieren
        registriereServiceBeobachter();

        // Beobachter erzeugen und an den Subwerkzeugen registrieren
        registriereSubWerkzeugBeobachter();

        // Die Vormerkaktionen werden erzeugt und an der UI registriert.
        registriereUIAktionen();
    }

    /**
     * Registriert die Aktionen, die bei benachrichtigungen der Services
     * ausgeführt werden.
     */
    private void registriereServiceBeobachter()
    {
        registriereVormerkButtonAktualisierenAktion();
        registriereVormerkungLoeschenButtonAktualisierenAktion();
    }

    /**
     * Registriert die Aktionen, die bei bestimmten Änderungen in Subwerkzeugen
     * ausgeführt werden.
     */
    private void registriereSubWerkzeugBeobachter()
    {
        registriereKundenAnzeigenAktion();
        registriereMedienAnzeigenAktion();
    }

    /**
     * Registriert die Aktionen, die bei bestimmten UI-Events ausgeführt werden.
     */
    private void registriereUIAktionen()
    {
        registriereVormerkAktion();
        registriereVormerkungLoeschenAktion();
    }

    /**
     * Registriert die Aktion zur Aktualisierung des Vormerkbuttons, wenn eine
     * Benachrichtigung vom Verleihservice auftaucht.
     */
    private void registriereVormerkButtonAktualisierenAktion()
    {
        _verleihService.registriereBeobachter(new ServiceObserver()
        {

            @Override
            public void reagiereAufAenderung()
            {
                aktualisiereVormerkButton();
                
            }
        });
    }
    
    /**
     * Registriert die Aktion zur Aktualisierung des Vormerkung Loeschen buttons, wenn eine
     * Benachrichtigung vom Verleihservice auftaucht.
     */
    private void registriereVormerkungLoeschenButtonAktualisierenAktion()
    {
        _verleihService.registriereBeobachter(new ServiceObserver()
        {

            @Override
            public void reagiereAufAenderung()
            {
                aktualisiereVormerkungLoeschenButton();
                
            }
        });
    }

    /**
     * Registriert die Aktion, die ausgeführt wird, wenn auf den Vormerk-Button
     * gedrückt wird.
     */
    private void registriereVormerkAktion()
    {
        _vormerkUI.getVormerkenButton()
            .addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    merkeAusgewaehlteMedienVor();
                }
            });
    }
    
    /**
     * Registriert die Aktion, die ausgeführt wird, wenn auf den Vormerkung Loeschen-Button
     * gedrückt wird.
     */
    private void registriereVormerkungLoeschenAktion()
    {
        _vormerkUI.getVormerkungLoeschenButton()
            .addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    loescheVormerkungenAusgewaehlterMedien();
                }
            });
    }

    /**
     * Registiert die Aktion, die ausgeführt wird, wenn ein Kunde ausgewählt
     * wird.
     */
    private void registriereKundenAnzeigenAktion()
    {
        _kundenAuflisterWerkzeug.registriereBeobachter(new SubWerkzeugObserver()
        {
            @Override
            public void reagiereAufAenderung()
            {
                zeigeAusgewaehltenKunden();
                aktualisiereVormerkButton();
                aktualisiereVormerkungLoeschenButton();
            }
        });
    }

    /**
     * Registiert die Aktion, die ausgeführt wird, wenn ein Medium ausgewählt
     * wird.
     */
    private void registriereMedienAnzeigenAktion()
    {
        _medienAuflisterWerkzeug.registriereBeobachter(new SubWerkzeugObserver()
        {

            @Override
            public void reagiereAufAenderung()
            {
                zeigeAusgewaehlteMedien();
                aktualisiereVormerkButton();
                aktualisiereVormerkungLoeschenButton();
            }
        });
    }

    /**
     * Überprüft, ob ein Kunde selektiert ist und ob selektierte Medien für
     * diesen Kunden vorgemerkt werden können.
     * 
     * @return true, wenn vormerken möglich ist, sonst false.
     */
    private boolean istVormerkenMoeglich()
    {
        List<Medium> medien = _medienAuflisterWerkzeug.getSelectedMedien();
        Kunde kunde = _kundenAuflisterWerkzeug.getSelectedKunde();
        // TODO für Aufgabenblatt 6 (nicht löschen): Prüfung muss noch eingebaut
        // werden. Ist dies korrekt imlpementiert, wird der Vormerk-Button gemäß
        // der Anforderungen a), b), c) und e) aktiviert. Note:erledigt, siehe hier:
        boolean vormerkenMoeglich = (kunde != null) && !medien.isEmpty() && 
                (_verleihService.istVormerkenMoeglich(kunde, medien));

        return vormerkenMoeglich;
    }
    
    /**
     * Überprüft, ob alle ausgewählten Medien von dem ausgewählten Kunden
     * vorgemerkt sind.
     * @return true, wenn alle vorgemerkt sind,
     *  false, sonst
     */
    private boolean sindAlleVorgemerktVon()
    {
        List<Medium> medien = _medienAuflisterWerkzeug.getSelectedMedien();
        Kunde kunde = _kundenAuflisterWerkzeug.getSelectedKunde();
        boolean sindAlleVorgemerktVon = (kunde != null) && !medien.isEmpty() && 
                _verleihService.sindAlleVorgemerktVon(kunde, medien);

        return sindAlleVorgemerktVon;
    }

    /**
     * Merkt die ausgewählten Medien für einen Kunden vor. Diese Methode wird
     * über einen Listener angestoßen, der reagiert, wenn der Benutzer den
     * VormerkButton drückt.
     */
    private void merkeAusgewaehlteMedienVor()
    {

        List<Medium> selectedMedien = _medienAuflisterWerkzeug
            .getSelectedMedien();
        Kunde selectedKunde = _kundenAuflisterWerkzeug.getSelectedKunde();
        try
        {
            _verleihService.merkeVor(selectedKunde, selectedMedien, Datum.heute());
        }
        catch (ProtokollierException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Fehlermeldung", JOptionPane.ERROR_MESSAGE);
        }

    }
    
    /**
     * Merkt die ausgewählten Medien für einen Kunden vor. Diese Methode wird
     * über einen Listener angestoßen, der reagiert, wenn der Benutzer den
     * VormerkButton drückt.
     */
    private void loescheVormerkungenAusgewaehlterMedien()
    {

        List<Medium> selectedMedien = _medienAuflisterWerkzeug
            .getSelectedMedien();
        Kunde selectedKunde = _kundenAuflisterWerkzeug.getSelectedKunde();
        try
        {
            _verleihService.loescheVormerkkarten(selectedMedien, Datum.heute(), selectedKunde);
        }
        catch (ProtokollierException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Fehlermeldung", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Zeigt die Details der ausgewählten Medien.
     */
    private void zeigeAusgewaehlteMedien()
    {
        List<Medium> selectedMedien = _medienAuflisterWerkzeug
            .getSelectedMedien();
        _medienDetailAnzeigerWerkzeug.setMedien(selectedMedien);
    }

    /**
     * Zeigt die Details des ausgewählten Kunden (rechts im Fenster)
     */
    private void zeigeAusgewaehltenKunden()
    {
        Kunde kunde = _kundenAuflisterWerkzeug.getSelectedKunde();
        _kundenDetailAnzeigerWerkzeug.setKunde(kunde);
    }

    /**
     * Setzt den Ausleihbutton auf benutzbar (enabled) falls die gerade
     * selektierten Medien alle ausgeliehen werden können und ein Kunde
     * ausgewählt ist.
     * 
     * Wenn keine Medien selektiert sind oder wenn mindestes eines der
     * selektierten Medien bereits ausgeliehen ist oder wenn kein Kunde
     * ausgewählt ist, wird der Button ausgegraut.
     */
    private void aktualisiereVormerkButton()
    {
        boolean istVormerkenMoeglich = istVormerkenMoeglich();
        _vormerkUI.getVormerkenButton()
            .setEnabled(istVormerkenMoeglich);
    }
    
    /**
     * Setzt den Vormerkung Loeschen Button auf benutzbar (enabled) falls die gerade
     * selektierten Medien alle von dem gewählten Kunden vorgemerkt sind.
     * 
     * Wenn keine Medien selektiert sind oder wenn mindestes eines der
     * selektierten Medien nicht von dem Kunden vorgemerkt ist, wird der Button ausgegraut.
     */
    private void aktualisiereVormerkungLoeschenButton()
    {
        boolean sindAlleVorgemerktVon = sindAlleVorgemerktVon();
        _vormerkUI.getVormerkungLoeschenButton()
            .setEnabled(sindAlleVorgemerktVon);
    }

    /**
     * Gibt das Panel, dass die UI-Komponente darstellt zurück.
     * 
     * @ensure result != null
     */
    public JPanel getUIPanel()
    {
        return _vormerkUI.getUIPanel();
    }
}
