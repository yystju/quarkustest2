package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.psplib.PSPLIBBaseListener;
import shi.quan.psplib.PSPLIBLexer;
import shi.quan.psplib.PSPLIBParser;
import shi.quan.vo.PSPData;

import java.io.IOException;
import java.io.InputStream;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);

    @Test
    public void test() throws IOException {
        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/j120.sm/j1201_1.sm");
//        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/example.sm");

        PSPData data = new PSPData();

        CharStream stream = CharStreams.fromStream(ins);

        PSPLIBLexer lexer = new PSPLIBLexer(stream);

        PSPLIBParser parser = new PSPLIBParser(new CommonTokenStream(lexer));

        parser.addParseListener(new PSPLIBBaseListener() {
            String current = "";
            String privateKey = null;


            @Override
            public void enterResource(PSPLIBParser.ResourceContext ctx) {
//                logger.info("[enterResource] {}", ctx.getText());
                current = "RESOURCE";
                super.enterResource(ctx);
            }

            @Override
            public void enterProjectInformation(PSPLIBParser.ProjectInformationContext ctx) {
//                logger.info("[enterProjectInformation] {}", ctx.getText());
                current = "PROJECTINFORMATION";
                super.enterProjectInformation(ctx);
            }

            @Override
            public void enterPrecedenceRelations(PSPLIBParser.PrecedenceRelationsContext ctx) {
                logger.info("[enterPrecedenceRelations] {}", ctx.getText());
                current = "PRECEDENCERELATIONS";
                super.enterPrecedenceRelations(ctx);
            }

            @Override
            public void exitKey(PSPLIBParser.KeyContext ctx) {
                logger.info("[exitKey] {}", ctx.getText());
                privateKey = ctx.getText();
                super.exitKey(ctx);
            }

            @Override
            public void exitValue(PSPLIBParser.ValueContext ctx) {
//                logger.info("[exitValue] {}", ctx.getText());

                String key = privateKey.trim();
                String value = ctx.getText().trim();

                if("RESOURCE".equals(current)) {
                    data.getResources().put(key, value);
                } else if("PROJECTINFORMATION".equals(current)) {

                } else if("PRECEDENCERELATIONS".equals(current)) {

                } else {
                    data.getProperties().put(key, value);
                }

                super.exitValue(ctx);
            }

            @Override
            public void exitHeader(PSPLIBParser.HeaderContext ctx) {
//                logger.info("[exitHeader] {}", ctx.getText());

                String header = ctx.getText().trim();

                if("RESOURCE".equals(current)) {

                } else if("PROJECTINFORMATION".equals(current)) {
                    data.setProjectInformationHeader(header);
                } else if("PRECEDENCERELATIONS".equals(current)) {
                    data.setPrecedenceRelationsHeader(header);
                } else {

                }

                super.exitHeader(ctx);
            }

            @Override
            public void exitRow(PSPLIBParser.RowContext ctx) {
//                logger.info("[exitRow] {}", ctx.getText());

                String row = ctx.getText().trim();

                if("RESOURCE".equals(current)) {

                } else if("PROJECTINFORMATION".equals(current)) {
                    data.getProjectInformationRows().add(row);
                } else if("PRECEDENCERELATIONS".equals(current)) {
                    data.getPprecedenceRelationsRows().add(row);
                } else {

                }

                super.exitRow(ctx);
            }

            @Override
            public void exitOthers(PSPLIBParser.OthersContext ctx) {
//                logger.info("[exitOthers] {}", ctx.getText());
                super.exitOthers(ctx);
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
//                logger.info("[visitErrorNode] {}", node.getText());
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
}