package mcpr.helops_client;

import mcpr.hellpops_interfaces.Incident;
import mcpr.hellpops_interfaces.IAuthService;
import mcpr.hellpops_interfaces.ITicketService;
import mcpr.hellpops_interfaces.Jeton;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

public class Console {

	public static void main(String[] args) {
		try {
			IAuthService auth = chargerAuth();
			ITicketService ticket = chargerTicket();

			Scanner scan = new Scanner(System.in);
			lancerMenuConnexion(scan, auth, ticket);
			scan.close();

		}
		catch (Exception e) {
			System.err.println("Erreur RMI : les serveurs sont-ils allumés ?");
			e.printStackTrace();
		}
	}

	private static IAuthService chargerAuth() throws Exception {
		return (IAuthService) Naming.lookup("//localhost/AuthService");
	}

	private static ITicketService chargerTicket() throws Exception {
		return (ITicketService) Naming.lookup("//localhost/TicketService");
	}

	private static void lancerMenuConnexion(Scanner scan, IAuthService auth, ITicketService ticket) throws Exception {

		boolean actif = true;

		while (actif) {

			afficherMenuConnexion();
			String choix = scan.nextLine();

			switch (choix) {

				case "1":
					gererInscription(scan, auth);
					break;

				case "2":
					Jeton jeton = gererConnexion(scan, auth);

					if (jeton != null) {
						gererMenuIncidents(scan, ticket, auth, jeton);
					}

					break;

				case "3":
					actif = false;
					System.out.println("Au revoir !");
					break;

				default:
					System.out.println("Choix invalide.");
			}
		}
	}

	private static void afficherMenuConnexion() {
		System.out.println("\n=== BIENVENUE SUR HELP'OPS ===");
		System.out.println("1. S'inscrire");
		System.out.println("2. Se connecter");
		System.out.println("3. Quitter");
		System.out.print("Choisissez une option : ");
	}

	private static void gererInscription(Scanner scan, IAuthService auth) throws Exception {

		boolean inscription_reussie = false;

		while (!inscription_reussie) {

			System.out.println("\n--- INSCRIPTION ---");

			System.out.print("Login : ");
			String login = scan.nextLine();

			System.out.print("Mot de passe : ");
			String mdp = scan.nextLine();

			boolean inscription = auth.inscription(login, mdp);

			if (!inscription) {
				System.out.println("Inscription refusée, login déjà existant !");
				inscription_reussie = false;
			}

			else {
				System.out.println("Inscription réussie !");
				inscription_reussie = true;
			}
		}
	}

	private static Jeton gererConnexion(Scanner scan, IAuthService auth) throws Exception {

		System.out.println("\n--- CONNEXION ---");

		System.out.print("Login : ");
		String login = scan.nextLine();

		System.out.print("Mot de passe : ");
		String password = scan.nextLine();

		Jeton jeton = auth.connexion(login, password);

		if (jeton != null) {
			System.out.println("Connexion réussie !");
		}

		else {
			System.out.println("Identifiants incorrects.");
		}

		return jeton;
	}

	private static void gererMenuIncidents(Scanner scan, ITicketService ticket, IAuthService auth, Jeton jeton) throws Exception {

		while (jeton != null) {

			System.out.println("\n=== MENU PRINCIPAL ===");
			System.out.println("1. Créer un ticket");
			System.out.println("2. Consulter mes tickets");
			System.out.println("3. Consulter les détails d'un ticket");
			System.out.println("4. Modifier un ticket");
			System.out.println("5. Se déconnecter");
			System.out.print("Choisissez une option : ");

			String choix = scan.nextLine();

			switch (choix) {

				case "1":
					creerTicket(scan, ticket, jeton);
					break;

				case "2":
					consulterTickets(ticket, jeton);
					break;

				case "3":
					consulterDetail(scan, ticket, jeton);
					break;

				case "4":
					modifierTicket(scan, ticket, jeton);
					break;

				case "5":
					auth.deconnexion(jeton);
					jeton = null;
					System.out.println("Déconnexion réussie.");
					break;

				default:
					System.out.println("Choix invalide.");
			}
		}
	}

	private static void creerTicket(Scanner scan, ITicketService ticket, Jeton jeton) throws Exception {

		System.out.println("\n--- CRÉER UN TICKET ---");

		String categorie = "", titre = "", description = "";

		while (categorie.trim().isEmpty()) {
			System.out.println("Catégorie : ");
			categorie = scan.nextLine();
		}

		while (titre.trim().isEmpty()) {
			System.out.println("Titre : ");
			titre = scan.nextLine();
		}

		while (description.trim().isEmpty()) {
			System.out.println("Description : ");
			description = scan.nextLine();
		}

		System.out.println(ticket.creerIncident(jeton, categorie, titre, description));
	}

	private static void consulterTickets(ITicketService ticket, Jeton jeton) throws Exception {

		System.out.println("\n--- MES TICKETS ---");

		List<Incident> tickets = ticket.consulterListeIncident(jeton);

		if (tickets != null && !tickets.isEmpty()) {
			tickets.forEach(System.out::println);
		} else {
			System.out.println("Vous n'avez aucun ticket.");
		}
	}

	private static int demanderIdValide(Scanner scan) {

		System.out.print("ID du ticket : ");

		try {
			return Integer.parseInt(scan.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Veuillez entrer un nombre valide.");
			return -1;
		}
	}

	private static void consulterDetail(Scanner scan, ITicketService ticket, Jeton jeton) throws Exception {

		System.out.println("\n--- DÉTAILS D'UN TICKET ---");

		int id = demanderIdValide(scan);
		if (id == -1) return;

		Incident incident = ticket.consulterIncidentDetail(jeton, id);

		if (incident != null) {
			System.out.println(incident);
			System.out.println("Description : " + incident.getDescription());
		} else {
			System.out.println("Ticket introuvable.");
		}
	}

	private static void modifierTicket(Scanner scan, ITicketService ticket, Jeton jeton) throws Exception {

		System.out.println("\n--- MODIFIER UN TICKET ---");

		int id = demanderIdValide(scan);
		if (id == -1) return;

		System.out.print("Nouvelle catégorie : ");
		String cat = scan.nextLine();

		System.out.print("Nouveau titre : ");
		String titre = scan.nextLine();

		System.out.print("Nouvelle description : ");
		String desc = scan.nextLine();

		Incident modifie = ticket.modifierIncident(jeton, id, cat, titre, desc);

		if (modifie != null) {
			System.out.println("Modification réussie !");
		} else {
			System.out.println("Échec modification.");
		}
	}


}