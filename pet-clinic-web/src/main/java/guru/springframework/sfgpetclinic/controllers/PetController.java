package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.model.Pet;
import guru.springframework.sfgpetclinic.model.PetType;
import guru.springframework.sfgpetclinic.services.OwnerService;
import guru.springframework.sfgpetclinic.services.PetService;
import guru.springframework.sfgpetclinic.services.PetTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {
    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

    private final PetService petService;
    private final OwnerService ownerService;
    private final PetTypeService petTypeService;

    public PetController(PetService petService, OwnerService ownerService, PetTypeService petTypeService) {
        this.petService = petService;
        this.ownerService = ownerService;
        this.petTypeService = petTypeService;
    }


    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes(){
        return petTypeService.findAll();
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable Long ownerId){
        return ownerService.findById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder){
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/pets/new")
    public String initCreationForm(Owner owner, Model model) {
        Pet pet = new Pet();
        if(owner.getPets() == null){
           Set<Pet> pets =  new HashSet<>();
           owner.setPets(pets);
        }
        owner.getPets().add(pet);
        pet.setOwner(owner);
        model.addAttribute("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model){
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(),true) != null){
            result.rejectValue("name", "duplicate","already exists");
        }
        if(owner.getPets() == null){
            Set<Pet> pets = new HashSet<>();
            owner.setPets(pets);
        }
        owner.getPets().add(pet);
        pet.setOwner(owner);
        if(result.hasErrors()){
            model.put("pet",pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }else{
            petService.save(pet);
            return "redirect:/owners/"+owner.getId();
        }
    }

    @GetMapping("/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable Long petId, Model model){
        model.addAttribute("pet",petService.findById(petId));
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, Model model){
        if(result.hasErrors()){
            model.addAttribute("pet",pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        }else{
            if (owner.getPets() == null){
                Set<Pet> pets = new HashSet<>();
                owner.setPets(pets);
            }
            Pet petFromDB = petService.findById(pet.getId()); // fetch the entity from the database
            Optional<Pet> optionalPet = Optional.of(petFromDB);
            if (optionalPet.isPresent()) {
                Pet existingPet = optionalPet.get();
                existingPet.setName(pet.getName()); // modify the entity
                existingPet.setBirthDate(pet.getBirthDate());
                existingPet.setPetType(pet.getPetType());
                existingPet.setVisits(pet.getVisits());
                existingPet.setOwner(owner);
                owner.getPets().add(pet);
                pet.setOwner(owner);
                petService.save(pet);

        }
            return "redirect:/owners/" + owner.getId();
        }
    }
}
