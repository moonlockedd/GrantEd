package repositories;

import data.interfaces.IDB;
import models.Choice;
import models.Question;
import repositories.interfaces.IChoiceRepository;
import repositories.interfaces.IQuestionRepository;

import java.sql.*;
import java.util.List;

public class QuestionRepository implements IQuestionRepository {
    private final IDB db;
    private final IChoiceRepository choiceRepo;

    public QuestionRepository(IDB db, ChoiceRepository choiceRepo) {
        this.db = db;
        this.choiceRepo = choiceRepo;
    }

    @Override
    public Question getQuestion(int id, String subject) {
        Question question = null;
        Connection con = null;

        try {
            // Establish connection
            con = db.getConnection();

            // List to store question choices
            List<Choice> choices = choiceRepo.getChoices(id, subject);

            // Prepare sql statement and execute it
            String sql = "SELECT question_text, explanation FROM questions " +
                    "WHERE id=? AND subject=?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setInt(1, id);
            st.setString(2, subject);

            ResultSet rs = st.executeQuery();

            if (rs.next() && choices != null) {
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
    public List<Question> getAllQuestions(String subject) {
        Connection con = null;

        try {
            // Write here!!!

        } catch (SQLException e) {
            System.out.println("SQL Exception: ");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Could not close connection: ");
                System.out.println(e.getMessage());
            }
        }

        return null;
    }
}
