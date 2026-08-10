package com.an.tripora.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp, String title) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(title);

            String html = """
                    <!DOCTYPE html>
                    <html lang="en">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">

                        <title>Verify your email — Tripora</title>
                    </head>

                    <body style="
                        margin: 0;
                        padding: 0;
                        background-color: #ffffff;
                        font-family:
                            'Plus Jakarta Sans',
                            Inter,
                            Arial,
                            Helvetica,
                            sans-serif;
                        color: #1f1f1f;
                    ">

                        <!-- Outer Container -->
                        <table
                            width="100%%"
                            cellpadding="0"
                            cellspacing="0"
                            border="0"
                            style="
                                width: 100%%;
                                background-color: #ffffff;
                                margin: 0;
                                padding: 0;
                            "
                        >
                            <tr>
                                <td
                                    align="center"
                                    style="
                                        padding: 48px 20px;
                                    "
                                >

                                    <!-- Email Card -->
                                    <table
                                        width="100%%"
                                        cellpadding="0"
                                        cellspacing="0"
                                        border="0"
                                        style="
                                            width: 100%%;
                                            max-width: 560px;
                                            background-color: #ffffff;
                                            border: 1px solid #e5e7eb;
                                            border-radius: 20px;
                                            overflow: hidden;
                                        "
                                    >

                                        <!-- Header -->
                                        <tr>
                                            <td
                                                style="
                                                    padding: 32px 36px;
                                                    border-bottom: 1px solid #e5e7eb;
                                                "
                                            >

                                                <table
                                                    width="100%%"
                                                    cellpadding="0"
                                                    cellspacing="0"
                                                    border="0"
                                                >
                                                    <tr>

                                                        <!-- Logo -->
                                                        <td
                                                            style="
                                                                font-size: 24px;
                                                                line-height: 1.4;
                                                                font-weight: 500;
                                                                letter-spacing: -0.5px;
                                                                color: #000000;
                                                            "
                                                        >
                                                            Tripora<span
                                                                style="
                                                                    color: #ff5a2f;
                                                                "
                                                            >.</span>
                                                        </td>

                                                        <!-- Badge -->
                                                        <td
                                                            align="right"
                                                        >
                                                            <span
                                                                style="
                                                                    display: inline-block;
                                                                    padding: 6px 12px;
                                                                    border: 1px solid #ff5a2f;
                                                                    border-radius: 30px;
                                                                    color: #ff5a2f;
                                                                    font-size: 11px;
                                                                    line-height: 1.4;
                                                                    font-weight: 600;
                                                                    letter-spacing: 0.4px;
                                                                "
                                                            >
                                                                VERIFICATION
                                                            </span>
                                                        </td>

                                                    </tr>
                                                </table>

                                            </td>
                                        </tr>


                                        <!-- Main Content -->
                                        <tr>
                                            <td
                                                style="
                                                    padding: 48px 36px 40px 36px;
                                                "
                                            >

                                                <!-- Eyebrow -->
                                                <div
                                                    style="
                                                        margin-bottom: 16px;
                                                        font-size: 12px;
                                                        line-height: 1.6;
                                                        font-weight: 600;
                                                        letter-spacing: 1.2px;
                                                        color: #707070;
                                                        text-transform: uppercase;
                                                    "
                                                >
                                                    Welcome to Tripora
                                                </div>


                                                <!-- Heading -->
                                                <h1
                                                    style="
                                                        margin: 0 0 16px 0;
                                                        padding: 0;
                                                        font-size: 32px;
                                                        line-height: 1.4;
                                                        font-weight: 400;
                                                        letter-spacing: -0.8px;
                                                        color: #1f1f1f;
                                                    "
                                                >
                                                    Verify your email.
                                                </h1>


                                                <!-- Description -->
                                                <p
                                                    style="
                                                        margin: 0 0 32px 0;
                                                        padding: 0;
                                                        font-size: 15px;
                                                        line-height: 1.64;
                                                        font-weight: 400;
                                                        color: #555555;
                                                    "
                                                >
                                                    Thanks for creating your Tripora
                                                    account. Enter the verification
                                                    code below to confirm your email
                                                    address and continue your journey.
                                                </p>


                                                <!-- OTP Container -->
                                                <table
                                                    width="100%%"
                                                    cellpadding="0"
                                                    cellspacing="0"
                                                    border="0"
                                                    style="
                                                        width: 100%%;
                                                        background-color: #f5f5f5;
                                                        border: 1px solid #e5e7eb;
                                                        border-radius: 20px;
                                                    "
                                                >
                                                    <tr>
                                                        <td
                                                            align="center"
                                                            style="
                                                                padding: 32px 20px;
                                                            "
                                                        >

                                                            <!-- OTP Label -->
                                                            <div
                                                                style="
                                                                    margin-bottom: 14px;
                                                                    font-size: 11px;
                                                                    line-height: 1.6;
                                                                    font-weight: 600;
                                                                    letter-spacing: 1.5px;
                                                                    color: #707070;
                                                                "
                                                            >
                                                                VERIFICATION CODE
                                                            </div>


                                                            <!-- OTP -->
                                                            <div
                                                                style="
                                                                    font-size: 40px;
                                                                    line-height: 1.2;
                                                                    font-weight: 600;
                                                                    letter-spacing: 8px;
                                                                    color: #000000;
                                                                    padding-left: 8px;
                                                                "
                                                            >
                                                                %s
                                                            </div>


                                                            <!-- Accent Line -->
                                                            <div
                                                                style="
                                                                    width: 32px;
                                                                    height: 3px;
                                                                    margin: 20px auto 0 auto;
                                                                    background-color: #ff5a2f;
                                                                    border-radius: 30px;
                                                                "
                                                            ></div>

                                                        </td>
                                                    </tr>
                                                </table>


                                                <!-- Expiration -->
                                                <table
                                                    width="100%%"
                                                    cellpadding="0"
                                                    cellspacing="0"
                                                    border="0"
                                                    style="
                                                        width: 100%%;
                                                        margin-top: 24px;
                                                    "
                                                >
                                                    <tr>

                                                        <td
                                                            valign="top"
                                                            style="
                                                                width: 24px;
                                                                padding-top: 2px;
                                                            "
                                                        >
                                                            <div
                                                                style="
                                                                    width: 8px;
                                                                    height: 8px;
                                                                    background-color: #ff5a2f;
                                                                    border-radius: 50%%;
                                                                "
                                                            ></div>
                                                        </td>

                                                        <td
                                                            style="
                                                                font-size: 13px;
                                                                line-height: 1.6;
                                                                color: #707070;
                                                            "
                                                        >
                                                            This code expires in
                                                            <strong
                                                                style="
                                                                    color: #333333;
                                                                    font-weight: 600;
                                                                "
                                                            >
                                                                5 minutes
                                                            </strong>.
                                                            For your security, please
                                                            do not share this code with
                                                            anyone.
                                                        </td>

                                                    </tr>
                                                </table>


                                                <!-- Divider -->
                                                <div
                                                    style="
                                                        height: 1px;
                                                        margin: 36px 0;
                                                        background-color: #e5e7eb;
                                                    "
                                                ></div>


                                                <!-- Security Message -->
                                                <p
                                                    style="
                                                        margin: 0;
                                                        font-size: 13px;
                                                        line-height: 1.6;
                                                        color: #707070;
                                                    "
                                                >
                                                    If you didn't create a Tripora
                                                    account, you can safely ignore
                                                    this email.
                                                </p>

                                            </td>
                                        </tr>


                                        <!-- Footer -->
                                        <tr>
                                            <td
                                                style="
                                                    padding: 28px 36px;
                                                    background-color: #f5f5f5;
                                                    border-top: 1px solid #e5e7eb;
                                                "
                                            >

                                                <table
                                                    width="100%%"
                                                    cellpadding="0"
                                                    cellspacing="0"
                                                    border="0"
                                                >
                                                    <tr>

                                                        <td
                                                            style="
                                                                font-size: 12px;
                                                                line-height: 1.6;
                                                                color: #707070;
                                                            "
                                                        >
                                                            © 2026 Tripora
                                                        </td>

                                                        <td
                                                            align="right"
                                                            style="
                                                                font-size: 12px;
                                                                line-height: 1.6;
                                                                color: #a5a5a5;
                                                            "
                                                        >
                                                            Explore more.
                                                        </td>

                                                    </tr>
                                                </table>

                                                <div
                                                    style="
                                                        margin-top: 12px;
                                                        font-size: 11px;
                                                        line-height: 1.6;
                                                        color: #a5a5a5;
                                                    "
                                                >
                                                    This is an automated message.
                                                    Please do not reply to this email.
                                                </div>

                                            </td>
                                        </tr>

                                    </table>

                                    <!-- Outside Footer -->
                                    <div
                                        style="
                                            max-width: 560px;
                                            padding: 20px 20px 0 20px;
                                            font-size: 11px;
                                            line-height: 1.6;
                                            color: #a5a5a5;
                                            text-align: center;
                                        "
                                    >
                                        Tripora — Your journey starts here.
                                    </div>

                                </td>
                            </tr>
                        </table>

                    </body>
                    </html>
                    """.formatted(otp);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Failed to send OTP email",
                    e
            );
        }
    }
}

