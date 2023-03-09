package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.service.AdminServiceImpl;
import pt.tecnico.distledger.server.domain.service.UserServiceImpl;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        boolean toDebug = false;
        ServerState serverState;

        System.out.println(ServerMain.class.getSimpleName());


        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Checks arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }

        // Converts the arguments
        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];
        if (args.length > 2 && args[2].equals("-debug")) {
            toDebug = true;
        }

        // Creates the ServerState and the services
        serverState = new ServerState(toDebug);
        final BindableService userImpl = new UserServiceImpl(toDebug, serverState);
        final BindableService adminImpl = new AdminServiceImpl(toDebug, serverState);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(userImpl).addService(adminImpl).build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();

    }

}

