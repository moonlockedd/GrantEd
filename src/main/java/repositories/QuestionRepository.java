package repositories;

import data.interfaces.IDB;
import exceptions.InvalidSubjectException;
import models.Choice;
import models.Question;
import repositories.interfaces.IChoiceRepository;
import repositories.interfaces.IQuestionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository implements IQuestionRepository {
    private final IDB db;
    private final IChoiceRepository choiceRepo;

    public QuestionRepository(IDB db, IChoiceRepository choiceRepo) {
        this.db = db;
        this.choiceRepo = choiceRepo;
    }

    @Override
    public Question getQuestion(String subject, int id) throws InvalidSubjectException{
        Question question = null;
        Connection con = null;

        try {
            // Check if a subject is valid
            List<String> subjects = getSubjectNames(true);
            subjects.addAll(getSubjectNames(false));
            convertToTableNames(subjects);

            if (!subjects.contains(subject)) {
                throw new InvalidSubjectException("Invalid subject");
            }

            // Establish connection
            con = db.getConnection();

            // Prepare SQL statement and execute it
            String sql = "SELECT question_text, explanation FROM " +
                    subject + " WHERE id=?";
            PreparedStatement st = con.prepareStatement(sql);

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            // Return new Question if question with that id is found
            if (rs.next()) {
                // List to store question choices
                List<Choice> choices = choiceRepo.getChoices(subject, id);
                question = new Question(
                        rs.getString("question_text"),
                        rs.getString("explanation"),
                        choices
                );
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception:");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection");
                System.out.println(e.getMessage());
            }
        }

        return question;
    }

    @Override
    public List<Question> getAllSubjectQuestions(String subject, boolean multi_answer) throws InvalidSubjectException {
        Connection con = null;

        try {
            // Check if a subject is valid
            List<String> subjects = getSubjectNames(true);
            subjects.addAll(getSubjectNames(false));
            convertToTableNames(subjects);

            if (!subjects.contains(subject)) {
                throw new InvalidSubjectException("Invalid subject");
            }

            // Establish connection
            con = db.getConnection();

            // ArrayList to store questions
            List<Question> questions = new ArrayList<>();

            // Prepare SQL statement and execute it
            String sql;
            if (!multi_answer)
                sql = "SELECT id, question_text, explanation FROM " +
                        subject + " WHERE ARRAY_LENGTH(correct_choices, 1)=1";
            else
                sql = "SELECT id, question_text, explanation FROM " +
                        subject + " WHERE ARRAY_LENGTH(correct_choices, 1)>1";

            PreparedStatement st = con.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            // Iterate through the result set
            while (rs.next()) {
                // Get choices for the current question id and subject
                List<Choice> choices = choiceRepo.getChoices(subject, rs.getInt("id"));

                // Create a new Question object
                Question question = new Question(
                        rs.getString("question_text"),
                        rs.getString("explanation"),
                        choices
                );

                // Add Question object to a list
                questions.add(question);
            }

            return questions;

        } catch (Exception e) {
            System.out.println("SQL Exception: ");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection");
                System.out.println(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public List<String> getSubjectNames(boolean elective) {
        Connection con = null;

        // Get all elective or mandatory subject table names
        try {
            // Establish connection
            con = db.getConnection();

            // Prepare SQL statement and execute it
            String sql = "SELECT subject FROM subjects WHERE is_elective=?";
            PreparedStatement st = con.prepareStatement(sql);

            st.setBoolean(1, elective);
            ResultSet rs = st.executeQuery();

            // List to store subject names
            List<String> subjects = new ArrayList<>();

            // Iterate through result set
            while (rs.next()) {
                subjects.add(rs.getString("subject"));
            }

            return subjects;

        } catch (SQLException e) {
            System.out.println("SQL Exception: ");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection");
                System.out.println(e.getMessage());
            }
        }

        return null;
    }

    public void convertToTableNames(List<String> subjects) {
        // Convert subject names into table names
        for (int i = 0; i < subjects.size(); i++) {
            subjects.set(i, subjects.get(i).toLowerCase());
            subjects.set(i, subjects.get(i).replaceAll(" ", "_"));
        }
    }
}
