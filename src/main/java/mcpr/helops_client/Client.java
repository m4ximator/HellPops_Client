package mcpr.helops_client;

import mcpr.hellpops_interfaces.Incident;
import mcpr.hellpops_interfaces.IAuthService;
import mcpr.hellpops_interfaces.ITicketService;
import mcpr.hellpops_interfaces.Jeton;

import java.rmi.Naming;
import java.util.Scanner;

public class Client {

	public static void main(String args[]) {
		try {
			IAuthService stubAuth = (IAuthService) Naming.lookup("//localhost/AuthService");
			ITicketService stubTicket = (ITicketService) Naming.lookup("//localhost/TicketService");
			Scanner scan = new Scanner(System.in);

			gererMenuConnexion(scan, stubAuth, stubTicket);

			scan.close();
		} catch (Exception exception) {
			System.err.println("Erreur RMI : les serveurs sont-ils allumés ?");
			exception.printStackTrace();
		}
	}

	private static void gererMenuConnexion(Scanner scan, IAuthService stubAuth, ITicketService stubTicket) throws Exception {
		boolean menuActif = true;

		while (menuActif) {
			System.out.println("\n=== BIENVENUE SUR HELP'OPS ===");
			System.out.println("1. S'inscrire");
			System.out.println("2. Se connecter");
			System.out.println("3. Quitter");
			System.out.print("Choisissez une option : ");

			String choix = scan.nextLine();

			switch (choix) {
				case "1":
					System.out.println("\n--- INSCRIPTION ---");

					boolean inscription_reussie = false;

					while (!inscription_reussie) {

						System.out.print("Login : ");
						String newLogin = scan.nextLine();
						System.out.print("Mot de passe : ");
						String newPassword = scan.nextLine();

						boolean inscription = stubAuth.inscription(newLogin, newPassword);

						if (!inscription) {
							System.out.println("Inscription refusée, login déjà existant !");
							inscription_reussie = false;
						}

						else {
							System.out.println("Inscription réussie !");
							inscription_reussie = true;
						}
					}

					break;

				case "2":
					System.out.println("\n--- CONNEXION ---");
					System.out.print("Login : ");
					String login = scan.nextLine();
					System.out.print("Mot de passe : ");
					String password = scan.nextLine();

					Jeton monJeton = stubAuth.connexion(login, password);

					if (monJeton != null) {
						System.out.println("Succès ! Vous êtes connecté.");
						gererMenuIncidents(scan, stubTicket, stubAuth, monJeton);
					} else {
						System.out.println("Échec : Identifiants incorrects.");
					}
					break;

				case "3":
					System.out.println("Au revoir !");
					menuActif = false;
					break;

				default:
					System.out.println("Choix invalide.");
					break;
			}
		}
	}

	private static void gererMenuIncidents(Scanner scan, ITicketService stubTicket, IAuthService stubAuth, Jeton monJeton) throws Exception {
		while (monJeton != null) {
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
					creerTicket(scan, stubTicket, monJeton);
					break;
				case "2":
					consulterTickets(stubTicket, monJeton);
					break;
				case "3":
					consulterDetail(scan, stubTicket, monJeton);
					break;
				case "4":
					modifierTicket(scan, stubTicket, monJeton);
					break;
				case "5":
					stubAuth.deconnexion(monJeton);
					monJeton = null;
					System.out.println("Vous êtes déconnecté.");
					break;
				default:
					System.out.println("Choix invalide.");
					break;
			}
		}
	}

	private static void creerTicket(Scanner scan, ITicketService stubTicket, Jeton monJeton) throws Exception {
		System.out.println("\n--- CRÉER UN TICKET ---");
		String categorie = "", titre = "", description = "";

		while (categorie.trim().isEmpty()) {
			System.out.print("Catégorie : ");
			categorie = scan.nextLine();
		}
		while (titre.trim().isEmpty()) {
			System.out.print("Titre : ");
			titre = scan.nextLine();
		}
		while (description.trim().isEmpty()) {
			System.out.print("Description : ");
			description = scan.nextLine();
		}
		System.out.println(stubTicket.creerIncident(monJeton, categorie, titre, description));
	}

	private static void consulterTickets(ITicketService stubTicket, Jeton monJeton) throws Exception {
		System.out.println("\n--- MES TICKETS ---");
		java.util.List<Incident> mesTickets = stubTicket.consulterListeIncident(monJeton);
		if (mesTickets != null && !mesTickets.isEmpty()) {
			mesTickets.forEach(System.out::println);
		} else {
			System.out.println("Vous n'avez aucun ticket.");
		}
	}

	private static void consulterDetail(Scanner scan, ITicketService stubTicket, Jeton monJeton) throws Exception {
		System.out.println("\n--- DÉTAILS D'UN TICKET ---");
		int id = demanderIdValide(scan);
		if (id == -1) return;

		Incident incident = stubTicket.consulterIncidentDetail(monJeton, id);
		if (incident != null) {
			System.out.println(incident.toString());
			System.out.println("Description : " + incident.getDescription());
		} else {
			System.out.println("Ticket introuvable ou accès refusé.");
		}
	}

	private static void modifierTicket(Scanner scan, ITicketService stubTicket, Jeton monJeton) throws Exception {
		System.out.println("\n--- MODIFIER UN TICKET ---");
		int id = demanderIdValide(scan);
		if (id == -1) return;

		System.out.println("(Laissez vide pour ne pas modifier)");
		System.out.print("Nouvelle Catégorie : ");
		String cat = scan.nextLine();
		System.out.print("Nouveau Titre : ");
		String titre = scan.nextLine();
		System.out.print("Nouvelle Description : ");
		String desc = scan.nextLine();

		Incident modifie = stubTicket.modifierIncident(monJeton, id, cat, titre, desc);
		if (modifie != null) {
			System.out.println("Modification réussie !");
			System.out.println(modifie.toString());
		}

		else {
			System.out.println("Échec : Ticket introuvable ou accès refusé.");
		}
	}

	// Méthode outil pour éviter que le parseInt ne fasse crasher le client
	private static int demanderIdValide(Scanner scan) {
		System.out.print("ID du ticket : ");
		try {
			return Integer.parseInt(scan.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Erreur : Veuillez entrer un nombre valide.");
			return -1;
		}
	}
}