package mcpr.helops_client;

import java.rmi.Naming;
import java.util.Scanner;
import mcpr.hellpops_interfaces.IAuthService;
import mcpr.hellpops_interfaces.Jeton;

public class Client {

	public static void main(String args[]) {
		try {
			// 1. Connexion au serveur RMI
			IAuthService stubAuth = (IAuthService) Naming.lookup("//localhost/AuthService");

			Scanner scan = new Scanner(System.in);
			boolean menuActif = true;
			Jeton monJeton = null;

			// 2. Préparation du menu avec StringBuilder
			StringBuilder affichageMenu = new StringBuilder();
			affichageMenu.append("\n=== BIENVENUE SUR HELP'OPS ===\n")
					.append("1. S'inscrire\n")
					.append("2. Se connecter\n")
					.append("3. Quitter\n")
					.append("Choisissez une option : ");

			// 3. La boucle du programme
			while (menuActif) {
				System.out.print(affichageMenu.toString());

				String choix = scan.nextLine();

				switch (choix) {
					case "1":
						System.out.println("\n--- INSCRIPTION ---");
						System.out.print("Choisissez un login : ");
						String newLogin = scan.nextLine();
						System.out.print("Choisissez un mot de passe : ");
						String newPassword = scan.nextLine();

						// Appel RMI pour l'inscription
						stubAuth.inscription(newLogin, newPassword);
						System.out.println("Inscription réussie ! Vous pouvez maintenant vous connecter.");
						break;

					case "2":
						System.out.println("\n--- CONNEXION ---");
						System.out.print("Login : ");
						String login = scan.nextLine();
						System.out.print("Mot de passe : ");
						String password = scan.nextLine();
                        
                        // Appel RMI pour la connexion, qui délivre un jeton
						monJeton = stubAuth.connexion(login, password);

						if (monJeton != null) {
							System.out.println("Succès ! Vous êtes connecté.");
							System.out.println("Votre Jeton : " + monJeton.getValeur());
							//menuActif = false; ligne pour ajout du menu pour le serveur incident
						} else {
							System.out.println("Échec : Identifiants incorrects.");
						}
						break;

					case "3":
						System.out.println("Au revoir !");
						menuActif = false; // Permet de sortir proprement de la boucle while
						break;

					default:
						System.out.println("Choix invalide, veuillez taper 1, 2 ou 3.");
						break;
				}
			}

			scan.close();

		} catch (Exception exception) {
			System.err.println("Erreur de connexion RMI : le serveur est-il bien allumé ?");
			exception.printStackTrace();
		}
	}
}