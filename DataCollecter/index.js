const { Client, GatewayIntentBits } = require('discord.js');
const fs = require('fs');

// --- CONFIGURATION ---
const BOT_TOKEN = 'MTU0Mjg0ODQzODUyMDI1ODY0MQ.Gzvr9s.9ZUj_nswbq2T8B0JyBdjgLF1b6l4WYHstbOmt4'; // 👈 Yahan apna Bot Token paste karo
const CHANNEL_ID = '1542844624136314924'; // 👈 Yahan apna Channel ID paste karo
// ---------------------

// Correct Intents Definition
const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent // Ye zaroori hai messages padhne ke liye
    ]
});

client.once('ready', () => {
    console.log(`✅ Logged in as ${client.user.tag}!`);
    console.log(`📂 Ready to receive commands.`);
});

client.on('messageCreate', message => {
    // Ignore messages from bots
    if (message.author.bot) return;

    if (message.content === '!sendtest') {
        try {
            const channel = client.channels.cache.get(CHANNEL_ID);
            if (channel) {
                channel.send('🚀 **Test Message Successful!** 🚀\n\nData collection is ready to start.')
                    .then(msg => {
                        console.log('✅ Message sent to Discord successfully!');
                    })
                    .catch(err => {
                        console.error('❌ Error sending message:', err);
                    });
            } else {
                message.reply('❌ Channel not found or bot doesn\'t have access. Make sure the bot is in the server.');
            }
        } catch (err) {
            console.error('❌ Error in message handler:', err);
        }
    }
});

client.login(BOT_TOKEN).catch(err => {
    console.error('❌ Login failed:', err);
});