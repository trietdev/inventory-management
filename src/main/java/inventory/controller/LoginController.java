package inventory.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import inventory.model.Auth;
import inventory.model.Menu;
import inventory.model.Role;
import inventory.model.UserRole;
import inventory.model.Users;
import inventory.service.UserService;
import inventory.util.Constant;
import inventory.validate.LoginValidator;

@Controller
public class LoginController {
	@Autowired
	private UserService userService;
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private LoginValidator loginValidatory;
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		if(binder.getTarget() == null) {return;}
		if(binder.getTarget().getClass() == Users.class) {
			binder.setValidator(loginValidatory);
		}
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("loginForm", new Users());
		return "login/login";
	}
	
	@PostMapping("/processLogin")
	public String processLogin(Model model, @ModelAttribute("loginForm")@Validated Users users, BindingResult bindingResult, HttpSession session) {
		if(bindingResult.hasErrors()) {
			return "login/login";
		}
		Users user = userService.findByProperty("userName", users.getUserName()).get(0);
		UserRole userRole = (UserRole) user.getUserRoles().iterator().next();
		List<Menu> menuList = new ArrayList<>();
		Role role = userRole.getRole();
		List<Menu> menuChildList = new ArrayList<>();
		for(Object obj : role.getAuths()) {
			Auth auth = (Auth) obj;
			Menu menu = auth.getMenu();
			if(menu.getParentId() == 0 && menu.getOrderIndex() != -1 && menu.getActiveFlag() == 1 && auth.getPermission() == 1 && auth.getActiveFlag() == 1) {
				menu.setIdMenu(menu.getUrl().replace("/", "") + "Id"); // /category/list ==> categorylistId
				menuList.add(menu);
			}else if(menu.getParentId() != 0 && menu.getOrderIndex() != -1 && menu.getActiveFlag() == 1 && auth.getPermission() == 1 && auth.getActiveFlag() == 1) {
				menu.setIdMenu(menu.getUrl().replace("/", "") + "Id");
				menuChildList.add(menu);
			}
		}
		for(Menu menu : menuList) {
			List<Menu> childList = new ArrayList<>();
			for(Menu childMenu : menuChildList) {
				System.out.println(childMenu.getName());
				if(childMenu.getParentId() == menu.getId()) {
					childList.add(childMenu);
				}
			}
			menu.setChild(childList);
		}
		sortMenu(menuList);
		for(Menu menu : menuList) {
			sortMenu(menu.getChild());
		}
		session.setAttribute(Constant.MENU_SESSION, menuList);
		session.setAttribute(Constant.USER_INFO, user);
		return "redirect:/index";
	}
	
	public void sortMenu(List<Menu> menus) {
		Collections.sort(menus, new Comparator<Menu>() {

			@Override
			public int compare(Menu o1, Menu o2) {
				// TODO Auto-generated method stub
				return o1.getOrderIndex() - o2.getOrderIndex();
			}
			
		});
	}
}
