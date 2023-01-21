package ppp.service.services;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;

import ppp.ServerConfig;
import ppp.auth.Emailer;
import ppp.db.controllers.CGames;
import ppp.db.controllers.CUser;
import ppp.db.model.OGame;
import ppp.db.model.OUser;
import ppp.meta.StatusEnum;
import ppp.service.AbstractService;

/**
 * Reminds users about their pending games. Email them once every day if they have a pending game older than a day
 */
public class EmailReminderService extends AbstractService {

	@Override
	public String getIdentifier() {
		return "email_reminder_service";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.DAYS.toMillis(1);
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {}

	@Override
	public void run() throws Exception {
		List<OGame> allPendingGames = CGames.getAllGamesByStatus(StatusEnum.Status.PENDING);
		Mailer emailer = Emailer.getMailer(true);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -24);
		Timestamp dayAgo = new Timestamp(calendar.getTimeInMillis());
		for (OGame game : allPendingGames) {
			if (game.date.after(dayAgo)) continue; // Only games older than a day
			System.out.println("Sending email for game id: " + game.id);
			OUser toUser = CUser.getCachedUser(game.receiver);
			OUser senderUser = CUser.getCachedUser(game.sender);
			Email email = EmailBuilder.startingBlank()
					.from("PingPongPage", ServerConfig.EMAIL_USER)
					.to(toUser.email)
				    .withSubject("Pending Game Reminder")
					.withHTMLText("Hey! You have a pending game against " + senderUser.username + " for " + game.winnerScore + "-" + game.loserScore + " on " + game.date.toString() + ".<br>Accept or decline it <a href=\"https://sppp.pro/games\">here.</a><br>&emsp;- Backend Software Engineer, Anthony Ford")
					.buildEmail();
			emailer.sendMail(email);
		}
	}

	@Override
	public void afterRun() {}

}
