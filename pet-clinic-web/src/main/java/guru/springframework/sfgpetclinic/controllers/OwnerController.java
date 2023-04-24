package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

@RequestMapping("/owners")
@Controller
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @RequestMapping("/find")
    public String findOwners(Model model){
        model.addAttribute("owner", Owner.builder().id(1L).build());
        return "owners/findOwners";
    }

    @GetMapping("/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") Long ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(ownerService.findById(ownerId));
        return mav;
    }

    @GetMapping()
    public String processFindForm(Owner owner, BindingResult bindingResult, Model model){
        if(owner.getLastName() == null){
            owner.setLastName("");
        }

        List<Owner> results = this.ownerService.findAllByLastNameLike("%" + owner.getLastName() + "%");
        if(results.isEmpty()){
            bindingResult.rejectValue("lastName","notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            owner=results.get(0);
            model.addAttribute("owners", results);
            return "redirect:/owners/" + owner.getId();
        }else {
            model.addAttribute("listOwners", results);
            return "owners/ownerList";
        }
    }
}