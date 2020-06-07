package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.psplib.PSPLIBBaseListener;
import shi.quan.psplib.PSPLIBLexer;
import shi.quan.psplib.PSPLIBParser;
import shi.quan.vo.PSPData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);
    public static final String RESOURCE = "RESOURCE";
    public static final String PROJECTINFORMATION = "PROJECTINFORMATION";
    public static final String PRECEDENCERELATIONS = "PRECEDENCERELATIONS";
    public static final String REQUESTSDURATIONS = "REQUESTSDURATIONS";
    public static final String RESOURCEAVAILABILITIES = "RESOURCEAVAILABILITIES";

    private static boolean verbose = false;

    @Test
    public void test() throws IOException {
        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/j120.sm/j1201_1.sm");

        PSPData data = new PSPData();

        Stack<String> stack = new Stack<>();

        CharStream stream = CharStreams.fromStream(ins);

        PSPLIBLexer lexer = new PSPLIBLexer(stream);

        PSPLIBParser parser = new PSPLIBParser(new CommonTokenStream(lexer));

        parser.addParseListener(new PSPLIBBaseListener() {
            String privateKey = null;

            @Override
            public void enterResource(PSPLIBParser.ResourceContext ctx) {
                if(verbose) logger.info("[enterResource] {}", ctx.getText());
                stack.push(RESOURCE);
                super.enterResource(ctx);
            }

            @Override
            public void exitResource(PSPLIBParser.ResourceContext ctx) {
                super.exitResource(ctx);
                stack.pop();
            }

            @Override
            public void enterProjectInformation(PSPLIBParser.ProjectInformationContext ctx) {
                if(verbose) logger.info("[enterProjectInformation] {}", ctx.getText());
                stack.push(PROJECTINFORMATION);
                super.enterProjectInformation(ctx);
            }

            @Override
            public void exitProjectInformation(PSPLIBParser.ProjectInformationContext ctx) {
                super.exitProjectInformation(ctx);
                stack.pop();
            }

            @Override
            public void enterPrecedenceRelations(PSPLIBParser.PrecedenceRelationsContext ctx) {
                if(verbose) logger.info("[enterPrecedenceRelations] {}", ctx.getText());
                stack.push(PRECEDENCERELATIONS);
                super.enterPrecedenceRelations(ctx);
            }

            @Override
            public void exitPrecedenceRelations(PSPLIBParser.PrecedenceRelationsContext ctx) {
                super.exitPrecedenceRelations(ctx);
                stack.pop();
            }

            @Override
            public void enterRequestsDurations(PSPLIBParser.RequestsDurationsContext ctx) {
                if(verbose) logger.info("[enterRequestsDurations] {}", ctx.getText());
                stack.push(REQUESTSDURATIONS);
                super.enterRequestsDurations(ctx);
            }

            @Override
            public void exitRequestsDurations(PSPLIBParser.RequestsDurationsContext ctx) {
                super.exitRequestsDurations(ctx);
            }

            @Override
            public void enterResourceAvailableilities(PSPLIBParser.ResourceAvailableilitiesContext ctx) {
                if(verbose) logger.info("[enterResourceAvailableilities] {}", ctx.getText());
                stack.push(RESOURCEAVAILABILITIES);
                super.enterResourceAvailableilities(ctx);
            }

            @Override
            public void exitResourceAvailableilities(PSPLIBParser.ResourceAvailableilitiesContext ctx) {
                super.exitResourceAvailableilities(ctx);
            }

            @Override
            public void exitKey(PSPLIBParser.KeyContext ctx) {
                if(verbose) logger.info("[exitKey] {}", ctx.getText());
                privateKey = ctx.getText();
                super.exitKey(ctx);
            }

            @Override
            public void exitValue(PSPLIBParser.ValueContext ctx) {
                if(verbose) logger.info("[exitValue] {}", ctx.getText());

                String key = privateKey.trim();
                String value = ctx.getText().trim();

                String current = stack.isEmpty() ? null : stack.peek();

                if(RESOURCE.equals(current)) {
                    data.getResources().put(key, value);
                } else if(PROJECTINFORMATION.equals(current)) {

                } else if(PRECEDENCERELATIONS.equals(current)) {

                } else {
                    data.getProperties().put(key, value);
                }

                super.exitValue(ctx);
            }

            @Override
            public void exitHeader(PSPLIBParser.HeaderContext ctx) {
                if(verbose) logger.info("[exitHeader] {}", ctx.getText());

                String current = stack.isEmpty() ? null : stack.peek();
                String header = ctx.getText().trim();

                if(RESOURCE.equals(current)) {

                } else if(PROJECTINFORMATION.equals(current)) {
                    String[] columns = parseHeader(ctx);
                    if(verbose) logger.info("[PROJECTINFORMATION] columns : {}, header : {}", columns, header);

                    data.setProjectInformationHeader(header);
                } else if(PRECEDENCERELATIONS.equals(current)) {
                    String[] columns = parseHeader(ctx);
                    if(verbose) logger.info("[PRECEDENCERELATIONS] columns : {}, header : {}", columns, header);

                    data.setPrecedenceRelationsHeader(header);
                } else {

                }

                super.exitHeader(ctx);
            }

            @Override
            public void exitRow(PSPLIBParser.RowContext ctx) {
                if(verbose) logger.info("[exitRow] {}", ctx.getText());
                String current = stack.isEmpty() ? null : stack.peek();
                String row = ctx.getText().trim();

                if("RESOURCE".equals(current)) {

                } else if(PROJECTINFORMATION.equals(current)) {
                    String[] columns = parseRow(ctx);
                    if(verbose) logger.info("[PROJECTINFORMATION] columns : {}, row : {}", columns, row);
                    data.getProjectInformationRows().add(row);
                } else if(PRECEDENCERELATIONS.equals(current)) {
                    String[] columns = parseRow(ctx);
                    if(verbose) logger.info("[PRECEDENCERELATIONS] columns : {}, row : {}", columns, row);
                    data.getPprecedenceRelationsRows().add(row);
                } else {

                }

                super.exitRow(ctx);
            }

            @Override
            public void exitOthers(PSPLIBParser.OthersContext ctx) {
                if(verbose) logger.info("[exitOthers] {}", ctx.getText());
                super.exitOthers(ctx);
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
                if(verbose) logger.info("[visitErrorNode] {}", node.getText());
                super.visitErrorNode(node);
            }
        });

        PSPLIBParser.ModelContext model = parser.model();

        ins.close();

        logger.info("properties : {}", data.getProperties());
        logger.info("resource : {}", data.getResources());
        logger.info("PrecedenceRelations : {}", data.getPrecedenceRelationsHeader());
        logger.info("PrecedenceRelations : {}", data.getPprecedenceRelationsRows());
    }

    private String[] parseRow(PSPLIBParser.RowContext ctx) {
        List<String> columnList = new ArrayList<>();

        for(int i = 0; i < ctx.getChildCount(); ++i) {
            String column = ctx.getChild(i).getText();

            column = column.trim();

            if(!"".equals(column)) {
                columnList.add(column);
            }
        }

        return columnList.toArray(new String[columnList.size()]);
    }

    private String[] parseHeader(PSPLIBParser.HeaderContext ctx) {
        List<String> columnList = new ArrayList<>();

        for(int i = 0; i < ctx.getChildCount(); ++i) {
            String column = ctx.getChild(i).getText();

            column = column.trim();

            if(!"".equals(column)) {
                columnList.add(column);
            }
        }

        return columnList.toArray(new String[columnList.size()]);
    }
}