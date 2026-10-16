/**
 * 
 */
package battleship;

public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args)
    {
		System.out.println("***  Battleship  ***");

		try {
			Tasks.menu();
		} catch (SqliteMoveRepository.PersistenceException e) {
			System.err.println("Erro de persistência: " + e.getMessage());

			if (e.getCause() != null) {
				System.err.println("Detalhe: " + e.getCause().getMessage());
			}

			System.err.println(
					"A sessão foi interrompida. As gravações anteriores permanecem na base de dados."
			);

			System.exit(1);
		}
    }
}
