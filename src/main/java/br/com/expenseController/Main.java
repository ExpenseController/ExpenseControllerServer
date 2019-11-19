package br.com.expenseController;

import br.com.expenseController.controller.PeriodController;
import br.com.expenseController.controller.PeriodControllerHelper;
import br.com.expenseController.controller.PeriodsControllerImpl;
import br.com.expenseController.controller.PeriodsControllerPOA;
import br.com.expenseController.controller.TagController;
import br.com.expenseController.controller.TagControllerHelper;
import br.com.expenseController.controller.TagsControllerImpl;
import br.com.expenseController.controller.TagsControllerPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Main {
    
    private static Main INSTANCE;
    private ORB orb;
    
    private Main() {
        try {
            java.util.Properties props = System.getProperties();
            props.put("org.omg.CORBA.ORBInitialPort", "1050");
            props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            props.put("com.sun.CORBA.giop.ORBGIOPVersion", "1.0");
            //Cria e inicializa o ORB
            orb = ORB.init(new String[]{}, props);
        } catch (Exception e) {
            System.err.println("ERRO: " + e);
            e.printStackTrace(System.out);
        }
    }
    
    public static Main getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Main();
        }
        
        return INSTANCE;
    }
    
    public void run() {
        // Aguarda chamadas dos clientes
        System.out.println("Server waiting connections ....");
        orb.run();
    }
    
    private void initControllers() {
        try {
            // Ativa o POA
            POA rootpoa = POAHelper.narrow(this.orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // Obtém uma referência para o servidor de nomes
            org.omg.CORBA.Object objRef = this.orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            
            // Cria a implementação e registra no ORB
            TagsControllerPOA impl = new TagsControllerImpl();
            // Pega a referência do servidor
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(impl);
            TagController href = TagControllerHelper.narrow(ref);
            // Registra o servidor no servico de nomes
            NameComponent path[] = ncRef.to_name("TagsController");
            ncRef.rebind(path, href);
            
            // Cria a implementação e registra no ORB
            PeriodsControllerPOA produtoImpl = new PeriodsControllerImpl();
            // Pega a referência do servidor
            ref = rootpoa.servant_to_reference(produtoImpl);
            PeriodController produtoHref = PeriodControllerHelper.narrow(ref);
            // Registra o servidor no servico de nomes
            path = ncRef.to_name("PeriodsController");
            ncRef.rebind(path, produtoHref);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public static void main(String args[]) {
        Main main = Main.getInstance();
        main.initControllers();
        main.run();
        System.out.println("Ending Server.");
    }

    public ORB getOrb() {
        return orb;
    }
}