package br.com.expenseController.controller;

import br.com.expenseController.Main;
import br.com.expenseController.model.Tag;
import br.com.expenseController.persistence.TagPersistence;
import br.com.expenseController.persistence.TagPersistenceHelper;
import java.util.List;
import java.util.Optional;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 *
 * @author Leandro Ramos (leandroramosmarcelino@hotmail.com)
 */
public class TagsControllerImpl extends TagsControllerPOA {

    private final TagPersistence persistence;

    public TagsControllerImpl() {
        this.persistence = getTagPersistenceRef();
    }
    
    @Override
    public boolean insert(Tag tag) {
        if (tag == null || tag.getDescription().isEmpty()) {
            return false;
        }
        
        List<Tag> tags = this.persistence.loadAll();
        
        if (tag.getCode() <= 0) {
            if (tags.stream().filter((t) -> t.getDescription().equals(tag.getDescription())).findFirst().isPresent()) {
                return false;
            }
            
            int code = tags.size() + 1;
            tag.setCode(code);
            tags.add(tag);
            return this.persistence.save(tags);
        } else {
            return update(tag);
        }
    }

    @Override
    public boolean update(Tag tag) {
        if (tag == null || tag.getDescription().isEmpty()) {
            return false;
        }
        
        List<Tag> tags = this.persistence.loadAll();
        
        if (tag.getCode() > 0) {
            final Optional<Tag> oldTag = tags.stream().filter((t) -> t.getCode() == tag.getCode()).findFirst();
        
            if (oldTag.isPresent()) {
                oldTag.get().setDescription(tag.getDescription());
                return this.persistence.save(tags);
            }
            
            return false;
        } else {
            return insert(tag);
        }
    }

    @Override
    public boolean remove(Tag tag) {
        if (tag == null || tag.getCode() < 1) {
            return false;
        }

        List<Tag> tags = this.persistence.loadAll();
        
        tags.stream().filter((t) -> t.getCode() == tag.getCode()).findFirst().ifPresent((t) -> {
            tags.remove(t);
            this.persistence.save(tags);
        });
        
        return true;
    }

    @Override
    public Tag load(int code) {
        if (code > 0) {
            return this.persistence.load(code);
        }
        
        return null;
    }
    
    private TagPersistence getTagPersistenceRef() {
        try {
            ORB orb = Main.getInstance().getOrb();
            //Obtem referencia para o servico de nomes
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            //Obtem referencia para o servidor
            return TagPersistenceHelper.narrow(ncRef.resolve_str("TagsPersistence"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
