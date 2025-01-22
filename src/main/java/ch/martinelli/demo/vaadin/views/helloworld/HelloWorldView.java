package ch.martinelli.demo.vaadin.views.helloworld;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@AnonymousAllowed
@Menu(order = 0, icon = LineAwesomeIconUrl.GLOBE_SOLID)
@PageTitle("Hello World")
@Route("")
public class HelloWorldView extends VerticalLayout {

    public HelloWorldView() {
        TextField nameTextField = new TextField("Your name");

        Button greetButton = new Button("Say hello");
        greetButton.addClickListener(e -> Notification.show("Hello %s".formatted(nameTextField.getValue()), 3000, Notification.Position.TOP_CENTER));
        greetButton.addClickShortcut(Key.ENTER);

        add(nameTextField, greetButton);
    }

}
