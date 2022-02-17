package coin_server;

public class Server_Main {
	public static void main(String[] args){
		System.out.println("java ver : 31");
		Server server = new Server();
		server.start_serving();
	}
}
