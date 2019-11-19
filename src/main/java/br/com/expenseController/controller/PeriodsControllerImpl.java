package br.com.expenseController.controller;

import br.com.expenseController.Main;
import br.com.expenseController.model.Period;
import br.com.expenseController.persistence.PeriodPersistence;
import br.com.expenseController.persistence.PeriodPersistenceHelper;
import java.util.List;
import java.util.Optional;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 *
 * @author Leandro Ramos (leandroramosmarcelino@hotmail.com)
 */
public class PeriodsControllerImpl extends PeriodsControllerPOA {

    private final PeriodPersistence persistence;

    public PeriodsControllerImpl() {
        this.persistence = getPeriodPersistenceRef();
    }
    
    @Override
    public boolean insert(Period period) {
        if (period == null || period.getDescription().isEmpty()) {
            return false;
        }
        
        List<Period> periods = this.persistence.loadAll();
        
        if (period.getCode() <= 0) {
            if (periods.stream().filter((t) -> t.getDescription().equals(period.getDescription())).findFirst().isPresent()) {
                return false;
            }
            
            int code = periods.size() + 1;
            period.setCode(code);
            periods.add(period);
            return this.persistence.save(periods);
        } else {
            return update(period);
        }
    }

    @Override
    public boolean update(Period period) {
        if (period == null || period.getDescription().isEmpty()) {
            return false;
        }
        
        List<Period> periods = this.persistence.loadAll();
        
        if (period.getCode() > 0) {
            final Optional<Period> oldPeriod = periods.stream().filter((p) -> p.getCode() == period.getCode()).findFirst();
        
            if (oldPeriod.isPresent()) {
                oldPeriod.get().setDescription(period.getDescription());
                return this.persistence.save(periods);
            }
            
            return false;
        } else {
            return insert(period);
        }
    }

    @Override
    public boolean remove(Period period) {
        if (period == null || period.getCode() < 1) {
            return false;
        }

        List<Period> periods = this.persistence.loadAll();
        
        periods.stream().filter((t) -> t.getCode() == period.getCode()).findFirst().ifPresent((t) -> {
            periods.remove(t);
            this.persistence.save(periods);
        });
        
        return true;
    }

    @Override
    public Period load(int code) {
        if (code > 0) {
            return this.persistence.load(code);
        }
        
        return null;
    }

    @Override
    public List<Period> loadAll() {
        return this.persistence.loadAll();
    }
    
    private PeriodPersistence getPeriodPersistenceRef() {
        try {
            ORB orb = Main.getInstance().getOrb();
            //Obtem referencia para o servico de nomes
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            //Obtem referencia para o servidor
            return PeriodPersistenceHelper.narrow(ncRef.resolve_str("TagsPersistence"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
