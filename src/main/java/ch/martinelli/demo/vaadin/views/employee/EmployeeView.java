package ch.martinelli.demo.vaadin.views.employee;

import ch.martinelli.demo.vaadin.domain.Employee;
import ch.martinelli.demo.vaadin.domain.EmployeeRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.DateFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

@RolesAllowed("ADMIN")
@PageTitle("Employees")
@Menu(order = 1, icon = LineAwesomeIconUrl.USERS_SOLID)
@Route("employees/:" + EmployeeView.ID + "?")
public class EmployeeView extends Div implements BeforeEnterObserver {

    static final String ID = "id";

    private final DateTimeFormatter dateFormatter;

    private final EmployeeRepository employeeRepository;

    private final Grid<Employee> grid = new Grid<>(Employee.class, false);
    private final BeanValidationBinder<Employee> binder = new BeanValidationBinder<>(Employee.class);

    private Employee employee;

    public EmployeeView(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", getLocale());

        setSizeFull();
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(75);
        splitLayout.addToPrimary(createGrid());
        splitLayout.addToSecondary(createEditor());

        add(splitLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> employeeId = event.getRouteParameters().get(ID).filter(StringUtils::isNumeric).map(Long::parseLong);
        if (employeeId.isPresent()) {
            Employee employee = employeeRepository.findById(employeeId.get()).orElseGet(() -> {
                Notification notification = new Notification("The requested employee was not found", 3000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();

                refreshGrid();
                event.forwardTo(EmployeeView.class);

                return new Employee();
            });
            populateForm(employee);
        } else {
            clearForm();
        }
    }

    private Grid<Employee> createGrid() {
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setMultiSort(true);

        grid.addColumn(Employee::getFirstName).setHeader("First Name").setSortable(true).setSortProperty("firstName");
        grid.addColumn(Employee::getLastName).setHeader("Last Name").setSortable(true).setSortProperty("lastName");
        grid.addColumn(e -> dateFormatter.format(e.getDateOfBirth())).setHeader("Date Of Birth").setSortable(true).setSortProperty("dateOfBirth");
        grid.addColumn(Employee::getEmail).setHeader("Email").setSortable(true).setSortProperty("email");
        grid.addColumn(Employee::getPhone).setHeader("Phone").setSortable(true).setSortProperty("phone");

        grid.setItems(query -> employeeRepository.findAll(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());

        grid.addSelectionListener(event -> event.getFirstSelectedItem()
                .ifPresent(employee -> UI.getCurrent().navigate(EmployeeView.class, new RouteParam(ID, employee.getId()))));

        return grid;
    }

    private VerticalLayout createEditor() {
        VerticalLayout editorlayout = new VerticalLayout();

        FormLayout formLayout = new FormLayout();

        TextField firstNameTextField = new TextField("First Name");
        binder.forField(firstNameTextField).bind("firstName");

        TextField lastNameTextField = new TextField("Last Name");
        binder.forField(lastNameTextField).bind("lastName");

        DatePicker dateOfBirthDatePicker = new DatePicker("Date Of Birth");
        dateOfBirthDatePicker.setLocale(getLocale());
        var symbols = new DateFormatSymbols(getLocale());
        var datePickerI18n = new DatePicker.DatePickerI18n();
        datePickerI18n.setDateFormat("dd.MM.yyyy");
        datePickerI18n.setMonthNames(Arrays.asList(symbols.getMonths()));
        datePickerI18n.setFirstDayOfWeek(1);
        datePickerI18n.setWeekdays(Arrays.stream(symbols.getWeekdays()).filter(s -> !s.isEmpty()).toList());
        datePickerI18n.setWeekdaysShort(Arrays.stream(symbols.getShortWeekdays()).filter(s -> !s.isEmpty()).toList());
        dateOfBirthDatePicker.setI18n(datePickerI18n);
        binder.forField(dateOfBirthDatePicker).bind("dateOfBirth");

        TextField emailField = new TextField("Email");
        binder.forField(emailField).bind("email");

        TextField phoneTextField = new TextField("Phone");
        binder.forField(phoneTextField).bind("phone");

        formLayout.add(firstNameTextField, lastNameTextField, dateOfBirthDatePicker, emailField, phoneTextField);

        editorlayout.add(formLayout);
        editorlayout.add(createButtons());

        return editorlayout;
    }

    private HorizontalLayout createButtons() {
        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (binder.writeBeanIfValid(employee)) {
                employeeRepository.save(employee);

                clearForm();
                refreshGrid();

                Notification notification = new Notification("Employee saved", 3000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.open();
                UI.getCurrent().navigate(EmployeeView.class);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.add(saveButton, cancelButton);

        return buttonLayout;
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(new Employee());
    }

    private void populateForm(Employee value) {
        employee = value;
        binder.readBean(employee);
    }
}
