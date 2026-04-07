package com.hotel.services;

import com.hotel.dao.CustomerDAO;
import com.hotel.models.Customer;

import java.sql.SQLException;
import java.util.*;

/**
 * Manages customer records via {@link CustomerDAO} and an in-memory list.
 */
public class CustomerService {

    private final ArrayList<Customer> customers = new ArrayList<>();
    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) throws SQLException {
        this.customerDAO = customerDAO;
        loadCustomersFromDb();
        if (customers.isEmpty()) {
            seedCustomersToDb();
        }
    }

    private void loadCustomersFromDb() throws SQLException {
        customers.clear();
        customers.addAll(customerDAO.findAll());
    }

    private void seedCustomersToDb() throws SQLException {
        addCustomer(new Customer("Ashwin Rao", "9876543210", 0));
        addCustomer(new Customer("Sanjana Patil", "9812345678", 0));
        addCustomer(new Customer("David Costa", "9900887766", 0));
        System.out.println("[CustomerService] Seeded customers into DB.");
    }

    public synchronized void addCustomer(Customer customer) throws SQLException {
        for (Customer c : customers) {
            if (c.getContactNumber().equals(customer.getContactNumber())) {
                throw new IllegalArgumentException("A customer with this contact number already exists.");
            }
        }
        customerDAO.insert(customer);
        customers.add(customer);
        System.out.println("[CustomerService] Added: " + customer);
    }

    public synchronized boolean removeCustomer(int customerId) throws SQLException {
        Iterator<Customer> it = customers.iterator();
        while (it.hasNext()) {
            Customer c = it.next();
            if (c.getCustomerId() == customerId) {
                it.remove();
                customerDAO.delete(customerId);
                System.out.println("[CustomerService] Removed customer ID: " + customerId);
                return true;
            }
        }
        return false;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> sorted = new ArrayList<>(customers);
        sorted.sort(Comparator.comparingInt(Customer::getCustomerId));
        return Collections.unmodifiableList(sorted);
    }

    public Customer findCustomerById(int customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId() == customerId) {
                return c;
            }
        }
        return null;
    }

    public Customer findCustomerByRoom(int roomNumber) {
        for (Customer c : customers) {
            if (c.getAllocatedRoomNumber() == roomNumber) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<Customer> getCustomersList() {
        return customers;
    }

    public synchronized void setCustomers(List<Customer> loaded) {
        customers.clear();
        customers.addAll(loaded);
    }

    public void reloadFromDatabase() throws SQLException {
        synchronized (this) {
            loadCustomersFromDb();
        }
    }
}
