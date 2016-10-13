package my.utility.winamp;

public class Main {

	public static void main(String[] args) {
		try {
			new WinAmpPlaylistExporterWindow().open();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
