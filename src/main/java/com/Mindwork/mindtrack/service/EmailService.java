package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.WeeklyReviewDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWeeklyReview(WeeklyReviewDto.WeeklyReport report) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );

            helper.setTo(report.getEmail());
            helper.setSubject("📊 Your MindTrack Weekly Review — "
                    + "Grade " + report.getPerformanceGrade());
            helper.setText(buildEmailHtml(report), true);

            mailSender.send(message);
            log.info("Weekly review sent to: {}", report.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send weekly review to {}: {}",
                    report.getEmail(), e.getMessage());
        }
    }

    private String buildEmailHtml(WeeklyReviewDto.WeeklyReport r) {
        String gradeColor = switch (r.getPerformanceGrade()) {
            case "S" -> "#34d399";
            case "A" -> "#60a5fa";
            case "B" -> "#a78bfa";
            default  -> "#fb923c";
        };

        String moodEmoji = switch (r.getDominantMood()) {
            case "GREAT"   -> "😄";
            case "GOOD"    -> "🙂";
            case "NEUTRAL" -> "😐";
            case "BAD"     -> "😕";
            case "AWFUL"   -> "😞";
            default        -> "📔";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>MindTrack Weekly Review</title>
            </head>
            <body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0"
                     style="background:#0a0a0f;padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0" cellspacing="0"
                           style="max-width:600px;width:100%%;">

                      <!-- Header -->
                      <tr>
                        <td style="background:#111118;border:1px solid #22222e;
                                   border-radius:12px 12px 0 0;padding:32px;
                                   text-align:center;">
                          <div style="font-size:28px;margin-bottom:8px;">◈</div>
                          <h1 style="margin:0;color:#f0f0f8;font-size:22px;
                                     font-weight:800;letter-spacing:-0.02em;">
                            MindTrack Weekly Review
                          </h1>
                          <p style="margin:8px 0 0;color:#8888a8;font-size:14px;">
                            %s — %s
                          </p>
                        </td>
                      </tr>

                      <!-- Grade Banner -->
                      <tr>
                        <td style="background:%s;padding:24px;text-align:center;">
                          <div style="font-size:48px;font-weight:900;color:#fff;
                                      letter-spacing:-0.04em;">%s</div>
                          <div style="color:rgba(255,255,255,0.85);font-size:14px;
                                      font-weight:600;margin-top:4px;">
                            WEEKLY PERFORMANCE GRADE
                          </div>
                        </td>
                      </tr>

                      <!-- Motivational message -->
                      <tr>
                        <td style="background:#111118;border-left:1px solid #22222e;
                                   border-right:1px solid #22222e;padding:24px 32px;">
                          <p style="margin:0;color:#f0f0f8;font-size:16px;
                                    line-height:1.6;font-style:italic;">
                            "%s"
                          </p>
                        </td>
                      </tr>

                      <!-- Stats Grid -->
                      <tr>
                        <td style="background:#111118;border-left:1px solid #22222e;
                                   border-right:1px solid #22222e;
                                   padding:8px 32px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                            </tr>
                            <tr>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                              <td width="33%%" style="padding:8px;">
                                %s
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Top Habit -->
                      <tr>
                        <td style="background:#111118;border-left:1px solid #22222e;
                                   border-right:1px solid #22222e;padding:0 32px 32px;">
                          <div style="background:#1a1a24;border:1px solid #22222e;
                                      border-radius:10px;padding:20px;">
                            <div style="color:#8888a8;font-size:11px;
                                        text-transform:uppercase;letter-spacing:0.08em;
                                        margin-bottom:8px;font-family:monospace;">
                              🔥 Top Habit This Week
                            </div>
                            <div style="color:#f0f0f8;font-size:18px;font-weight:700;">
                              %s
                            </div>
                            <div style="color:#7c6af7;font-size:13px;margin-top:4px;">
                              %d day streak
                            </div>
                          </div>
                        </td>
                      </tr>

                      <!-- Mood -->
                      <tr>
                        <td style="background:#111118;border-left:1px solid #22222e;
                                   border-right:1px solid #22222e;padding:0 32px 32px;">
                          <div style="background:#1a1a24;border:1px solid #22222e;
                                      border-radius:10px;padding:20px;text-align:center;">
                            <div style="color:#8888a8;font-size:11px;
                                        text-transform:uppercase;letter-spacing:0.08em;
                                        margin-bottom:8px;font-family:monospace;">
                              Dominant Mood This Week
                            </div>
                            <div style="font-size:36px;">%s</div>
                            <div style="color:#f0f0f8;font-size:16px;
                                        font-weight:700;margin-top:4px;">
                              %s
                            </div>
                          </div>
                        </td>
                      </tr>

                      <!-- CTA -->
                      <tr>
                        <td style="background:#111118;border-left:1px solid #22222e;
                                   border-right:1px solid #22222e;
                                   padding:0 32px 32px;text-align:center;">
                          <a href="http://localhost:5173/dashboard"
                             style="display:inline-block;background:#7c6af7;
                                    color:#fff;text-decoration:none;padding:14px 32px;
                                    border-radius:8px;font-weight:700;font-size:15px;">
                            Open MindTrack →
                          </a>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#0d0d14;border:1px solid #22222e;
                                   border-radius:0 0 12px 12px;padding:24px 32px;
                                   text-align:center;">
                          <p style="margin:0;color:#55556a;font-size:12px;">
                            MindTrack · Mental Discipline & Focus App<br/>
                            You're receiving this because you're a MindTrack user.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                r.getWeekStart().toString(),
                r.getWeekEnd().toString(),
                gradeColor,
                r.getPerformanceGrade(),
                r.getMotivationalMessage(),
                statCard("◧", String.valueOf(r.getHabitsCompletedThisWeek()),
                        "Habits Done"),
                statCard("📊", r.getHabitCompletionPercent() + "%",
                        "Completion Rate"),
                statCard("◔", r.getFocusMinutesThisWeek() + " min",
                        "Focus Time"),
                statCard("◫", String.valueOf(r.getJournalEntriesThisWeek()),
                        "Journal Entries"),
                statCard("⭐", "+" + r.getXpEarnedThisWeek() + " XP",
                        "XP Earned"),
                statCard("🔥", String.valueOf(r.getBestStreakThisWeek()),
                        "Best Streak"),
                r.getTopHabitName(),
                r.getTopHabitStreak(),
                moodEmoji,
                r.getDominantMood()
        );
    }

    private String statCard(String icon, String value, String label) {
        return """
            <div style="background:#1a1a24;border:1px solid #22222e;
                        border-radius:8px;padding:16px;text-align:center;">
              <div style="font-size:20px;margin-bottom:6px;">%s</div>
              <div style="color:#f0f0f8;font-size:20px;font-weight:800;
                          letter-spacing:-0.03em;font-family:monospace;">%s</div>
              <div style="color:#55556a;font-size:11px;text-transform:uppercase;
                          letter-spacing:0.08em;margin-top:4px;">%s</div>
            </div>
            """.formatted(icon, value, label);
    }
}